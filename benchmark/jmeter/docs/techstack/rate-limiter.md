

# RateLimiter

Getting started with resilience4j-ratelimiter

## Introduction

Rate limiting là kỹ thuật bắt buộc để chuẩn bị API cho khả năng mở rộng và đảm bảo tính sẵn sàng cao cùng độ tin cậy cho dịch vụ. Kỹ thuật này có nhiều lựa chọn về cách xử lý khi vượt ngưỡng: từ chối yêu cầu vượt quá, xếp hàng để thực thi sau, kết hợp cả hai. 

## Internals

Resilience4j cung cấp một RateLimiter chia tất cả nano giây kể từ epoch thành các chu kỳ. Mỗi chu kỳ có độ dài được cấu hình bởi RateLimiterConfig.limitRefreshPeriod. Ở đầu mỗi chu kỳ, RateLimiter đặt lại số quyền (permissions) khả dụng về giá trị RateLimiterConfig.limitForPeriod.

[
![](https://files.readme.io/44ca055-rate_limiter.png "ratelimiter.png")


Triển khai mặc định là AtomicRateLimiter, có một số tối ưu để bỏ qua việc refresh nếu RateLimiter không được sử dụng tích cực. AtomicRateLimiter quản lý trạng thái bằng AtomicReference. Trạng thái bất biến AtomicRateLimiter.State gồm các trường:

* `activeCycle` - số chu kỳ được dùng bởi lần gọi cuối cùng
* `activePermissions` - số quyền khả dụng sau lần gọi cuối cùng (có thể âm nếu một số quyền đã được đặt trước)
* `nanosToWait` - số nanô giây cần chờ để có quyền cho lần gọi cuối cùng

Ngoài ra còn có SemaphoreBasedRateLimiter dùng Semaphore và một scheduler để refresh quyền sau mỗi RateLimiterConfig#limitRefreshPeriod.

## Create a RateLimiterRegistry

Bạn có thể tạo một RateLimiterRegistry trong bộ nhớ để quản lý (tạo và lấy) các thể hiện RateLimiter.

```java

RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();",

```

## Create and configure a RateLimiter

Bạn cũng có thể cung cấp một cấu hình toàn cục tùy chỉnh bằng RateLimiterConfig. Các thuộc tính chính:

| Thuộc tính             | Giá trị mặc định | Mô tả |
|------------------------|------------------|--|
| timeoutDuration   | 5 s              | Thời gian chờ mặc định mà một luồng chờ để lấy permission |
| limitRefreshPeriod   | 500 ns              | Chu kỳ làm mới quyền; sau mỗi chu kỳ, số quyền được đặt lại về limitForPeriod |
| limitForPeriod   | 50                 | Số quyền khả dụng trong một chu kỳ làm mới |

Ví dụ: giới hạn tần suất gọi không vượt quá 10 req/ms:

```java

RateLimiterConfig config = RateLimiterConfig.custom()
  .limitRefreshPeriod(Duration.ofMillis(1))
  .limitForPeriod(10)
  .timeoutDuration(Duration.ofMillis(25))
  .build();

// Create registry
RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(config);

// Use registry
RateLimiter rateLimiterWithDefaultConfig = rateLimiterRegistry
  .rateLimiter("name1");

RateLimiter rateLimiterWithCustomConfig = rateLimiterRegistry
  .rateLimiter("name2", config);

```

Bạn có thể thay đổi cấu hình runtime bằng các phương thức như changeTimeoutDuration và changeLimitForPeriod. Lưu ý:

Thay đổi timeoutDuration sẽ không ảnh hưởng tới các luồng đang chờ permission.

Thay đổi limitForPeriod sẽ không ảnh hưởng tới quyền của chu kỳ hiện tại; giá trị mới áp dụng từ chu kỳ tiếp theo.

Ví dụ thay đổi giới hạn trong runtime:

```java

// during second refresh cycle limiter will get 100 permissions
rateLimiter.changeLimitForPeriod(100);

```

## Decorate and execute a functional interface

RateLimiter cung cấp các hàm decorate tương tự CircuitBreaker. Bạn có thể decorate bất kỳ Callable, Supplier, Runnable, Consumer, CheckedRunnable, CheckedSupplier, CheckedConsumer hoặc CompletionStage với một RateLimiter.

```java

// Decorate your call to BackendService.doSomething()
CheckedRunnable restrictedCall = RateLimiter
    .decorateCheckedRunnable(rateLimiter, backendService::doSomething);

Try.run(restrictedCall)
    .andThenTry(restrictedCall)
    .onFailure((RequestNotPermitted throwable) -> LOG.info("Wait before call it again :)"));


```

Khi một cuộc gọi bị từ chối do không có permission, RateLimiter sẽ ném RequestNotPermitted. Bạn có thể bắt và xử lý ngoại lệ này để thực hiện logic retry, xếp hàng, hoặc trả về lỗi phù hợp cho client.

## Consume emitted RegistryEvents

Khi một cuộc gọi bị từ chối do không có permission, RateLimiter sẽ ném RequestNotPermitted. Bạn có thể bắt và xử lý ngoại lệ này để thực hiện logic retry, xếp hàng, hoặc trả về lỗi phù hợp cho client.

```java

RateLimiterRegistry registry = RateLimiterRegistry.ofDefaults();
registry.getEventPublisher()
  .onEntryAdded(entryAddedEvent -> {
    RateLimiter addedRateLimiter = entryAddedEvent.getAddedEntry();
    LOG.info("RateLimiter {} added", addedRateLimiter.getName());
  })
  .onEntryRemoved(entryRemovedEvent -> {
    RateLimiter removedRateLimiter = entryRemovedEvent.getRemovedEntry();
    LOG.info("RateLimiter {} removed", removedRateLimiter.getName());
  });


```

## RateLimiterEvents

RateLimiter phát ra luồng RateLimiterEvents. Một event có thể là acquire thành công hoặc acquire thất bại. Mỗi event chứa thông tin bổ sung như thời gian tạo event và tên rate limiter. Để tiêu thụ event, đăng ký event consumer:

```java

rateLimiter.getEventPublisher()
    .onSuccess(event -> logger.info(...))
        .onFailure(event -> logger.info(...));

```

Bạn có thể chuyển EventPublisher sang luồng phản ứng bằng adapter RxJava, RxJava2 hoặc Project Reactor. Ví dụ với Reactor:

```java

ReactorAdapter.toFlux(rateLimiter.getEventPublisher())
    .filter(event -> event.getEventType() == FAILED_ACQUIRE)
    .subscribe(event -> logger.info(...));

```

```java

RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.custom()
.withRegistryStore(new CacheRateLimiterRegistryStore())
.build();

```