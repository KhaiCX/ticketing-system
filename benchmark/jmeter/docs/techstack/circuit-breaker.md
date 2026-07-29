

# CircuitBreaker

Getting started with resilience4j-circuitbreaker

## Introduction

CircuitBreaker được triển khai dưới dạng một máy trạng thái hữu hạn với ba trạng thái thông thường: CLOSED, OPEN và HALF_OPEN, và ba trạng thái đặc biệt METRICS_ONLY, DISABLED và FORCED_OPEN.

![](https://files.readme.io/39cdd54-state_machine.jpg "state_machine.jpg")

CircuitBreaker sử dụng một cửa sổ trượt (sliding window) để lưu trữ và tổng hợp kết quả của các cuộc gọi. Bạn có thể chọn giữa cửa sổ trượt theo số lượng (count-based) và theo thời gian (time-based). Cửa sổ theo số lượng tổng hợp kết quả của N cuộc gọi gần nhất. Cửa sổ theo thời gian tổng hợp kết quả của các cuộc gọi trong N giây gần nhất.

## Count-based sliding window

Cửa sổ trượt theo số lượng được triển khai bằng một mảng vòng gồm N phép đo.
Nếu kích thước cửa sổ là 10, mảng vòng luôn có 10 phép đo.
Cửa sổ trượt cập nhật dần một tổng hợp toàn cục. Tổng hợp này được cập nhật khi một kết quả cuộc gọi mới được ghi nhận. Khi phép đo cũ nhất bị loại bỏ, phép đo đó sẽ bị trừ khỏi tổng hợp toàn cục và bucket (ô) được đặt lại. (Cơ chế: Subtract-on-Evict)

Thời gian để lấy một Snapshot là hằng số O(1), vì Snapshot đã được tổng hợp trước và không phụ thuộc vào kích thước cửa sổ.
Yêu cầu bộ nhớ của triển khai này là O(n).

## Time-based sliding window

Cửa sổ trượt theo thời gian được triển khai bằng một mảng vòng gồm N tổng hợp từng phần (buckets).
Nếu kích thước cửa sổ thời gian là 10 giây, mảng vòng luôn có 10 bucket. Mỗi bucket tổng hợp kết quả của tất cả các cuộc gọi xảy ra trong một giây epoch nhất định. (Tổng hợp từng phần). Bucket đầu (head) của mảng vòng lưu trữ kết quả của giây epoch hiện tại. Các bucket khác lưu trữ kết quả của các giây trước đó.

Cửa sổ trượt không lưu từng kết quả cuộc gọi (tuple) riêng lẻ, mà cập nhật dần các tổng hợp từng phần (bucket) và một tổng hợp toàn cục. Tổng hợp toàn cục được cập nhật dần khi một kết quả cuộc gọi mới được ghi nhận. Khi bucket cũ nhất bị loại bỏ, tổng hợp của bucket đó sẽ bị trừ khỏi tổng hợp toàn cục và bucket được đặt lại. (Cơ chế: Subtract-on-Evict)

Thời gian để lấy một Snapshot là hằng số O(1), vì Snapshot đã được tổng hợp trước và không phụ thuộc vào kích thước cửa sổ thời gian.
Yêu cầu bộ nhớ của triển khai này gần như hằng số O(n), vì các kết quả cuộc gọi không được lưu riêng lẻ. Chỉ có N tổng hợp từng phần và 1 tổng hợp toàn cục được tạo.

Một tổng hợp từng phần gồm 3 số nguyên để đếm: số cuộc gọi thất bại, số cuộc gọi chậm và tổng số cuộc gọi; và một long lưu trữ tổng thời lượng của tất cả các cuộc gọi.

## Failure rate and slow call rate thresholds

Trạng thái của CircuitBreaker chuyển từ CLOSED sang OPEN khi tỷ lệ lỗi bằng hoặc lớn hơn một ngưỡng cấu hình. Ví dụ: khi hơn 50% các cuộc gọi đã ghi nhận bị lỗi.
Mặc định, tất cả ngoại lệ được tính là lỗi. Bạn có thể định nghĩa một danh sách ngoại lệ được tính là lỗi; các ngoại lệ khác sẽ được tính là thành công, trừ khi chúng bị ignore. Ngoại lệ cũng có thể được bỏ qua để chúng không được tính là lỗi hay thành công.

CircuitBreaker cũng chuyển từ CLOSED sang OPEN khi tỷ lệ cuộc gọi chậm bằng hoặc lớn hơn ngưỡng cấu hình. Ví dụ: khi hơn 50% các cuộc gọi mất hơn 5 giây. Điều này giúp giảm tải lên hệ thống bên ngoài trước khi nó thực sự không phản hồi.

Tỷ lệ lỗi và tỷ lệ cuộc gọi chậm chỉ có thể được tính nếu số lượng cuộc gọi tối thiểu đã được ghi nhận. Ví dụ, nếu số cuộc gọi tối thiểu là 10, thì ít nhất phải có 10 cuộc gọi được ghi nhận trước khi tỷ lệ lỗi có thể được tính. Nếu chỉ có 9 cuộc gọi, CircuitBreaker sẽ không chuyển sang open ngay cả khi cả 9 cuộc gọi đều thất bại.

Khi CircuitBreaker ở trạng thái OPEN, các cuộc gọi bị từ chối với ngoại lệ CallNotPermittedException. Sau khi thời gian chờ (wait time duration) trôi qua, CircuitBreaker chuyển từ OPEN sang HALF_OPEN và cho phép một số lượng cuộc gọi cấu hình trước được thực hiện để kiểm tra xem backend còn bị lỗi hay đã phục hồi. Các cuộc gọi tiếp theo bị từ chối với CallNotPermittedException cho đến khi tất cả các cuộc gọi được phép hoàn thành.
Nếu tỷ lệ lỗi hoặc tỷ lệ cuộc gọi chậm sau đó bằng hoặc lớn hơn ngưỡng, trạng thái chuyển lại OPEN. Nếu cả hai tỷ lệ đều dưới ngưỡng, trạng thái chuyển về CLOSED.

CircuitBreaker hỗ trợ thêm ba trạng thái đặc biệt: METRICS_ONLY (luôn cho phép truy cập), DISABLED (luôn cho phép truy cập) và FORCED_OPEN (luôn từ chối truy cập). Ở trạng thái METRICS_ONLY, tất cả sự kiện CircuitBreaker (ngoại trừ chuyển trạng thái) vẫn được phát và metrics được ghi nhận, tương tự như trạng thái CLOSED nhưng circuit sẽ không mở khi vượt ngưỡng. Ở trạng thái DISABLED và FORCED_OPEN, không có CircuitBreakerEvents (ngoại trừ chuyển trạng thái) được phát và không có metrics nào được ghi nhận. Cách duy nhất để thoát khỏi các trạng thái này là kích hoạt một chuyển trạng thái hoặc reset CircuitBreaker.

CircuitBreaker là an toàn cho đa luồng (thread-safe) như sau:

Trạng thái của CircuitBreaker được lưu trong một AtomicReference.

CircuitBreaker sử dụng các phép toán nguyên tử để cập nhật trạng thái với các hàm không có tác dụng phụ.

Ghi nhận cuộc gọi và đọc snapshot từ Sliding Window được đồng bộ hóa.

Điều này đảm bảo tính nguyên tử và chỉ một luồng có thể cập nhật trạng thái hoặc Sliding Window tại một thời điểm.

Tuy nhiên, CircuitBreaker không đồng bộ hóa chính lời gọi hàm. Điều này có nghĩa là lời gọi hàm không nằm trong phần critical section. Nếu không, CircuitBreaker sẽ gây ra tắc nghẽn hiệu năng lớn. Một lời gọi hàm chậm sẽ ảnh hưởng tiêu cực đến hiệu năng/throughput tổng thể.

Nếu 20 luồng đồng thời yêu cầu quyền thực thi một hàm và trạng thái CircuitBreaker là CLOSED, tất cả 20 luồng đều được phép gọi hàm, ngay cả khi kích thước sliding window là 15. Sliding window không giới hạn số lượng cuộc gọi đồng thời. Nếu bạn muốn giới hạn số luồng đồng thời, hãy dùng Bulkhead. Bạn có thể kết hợp Bulkhead và CircuitBreaker.

Example with 1 Thread:

![](https://files.readme.io/45dc011-Thread1.PNG "Thread1.PNG")

Example with 3 Threads:

![](https://files.readme.io/8d10418-Multiplethreads.PNG "Multiplethreads.PNG")

## Create a CircuitBreakerRegistry

Resilience4j cung cấp một CircuitBreakerRegistry lưu trong bộ nhớ dựa trên ConcurrentHashMap đảm bảo an toàn đa luồng và tính nguyên tử. Bạn có thể dùng CircuitBreakerRegistry để quản lý (tạo và lấy) các thể hiện CircuitBreaker. Bạn có thể tạo một CircuitBreakerRegistry với cấu hình mặc định toàn cục như sau:

```java
CircuitBreakerRegistry circuitBreakerRegistry = 
  CircuitBreakerRegistry.ofDefaults();
```

## Create and configure a CircuitBreaker

Bạn có thể cung cấp CircuitBreakerConfig mặc định toàn cục tùy chỉnh. Để tạo cấu hình tùy chỉnh, dùng builder CircuitBreakerConfig. Builder cho phép cấu hình các thuộc tính sau.

| Thuộc tính             | Giá trị mặc định | Mô tả |
|------------------------|------------------|--|
| failureRateThreshold   | 50               | Ngưỡng tỷ lệ lỗi (%) — khi tỷ lệ lỗi ≥ ngưỡng, CircuitBreaker chuyển sang open. |
| slowCallRateThreshold   | 100              | Ngưỡng tỷ lệ cuộc gọi chậm (%) — khi tỷ lệ cuộc gọi chậm ≥ ngưỡng, CircuitBreaker chuyển sang open. |
| slowCallDurationThreshold   | 60000 [ms]                 | Ngưỡng thời lượng để coi một cuộc gọi là chậm. |
| permittedNumberOfCallsInHalfOpenState   | 10               | Số cuộc gọi được phép khi ở HALF_OPEN. |
| maxWaitDurationInHalfOpenState   | 0 [ms]           | Thời gian chờ tối đa ở HALF_OPEN trước khi chuyển về OPEN; 0 nghĩa là chờ vô hạn cho đến khi các cuộc gọi được phép hoàn thành. |
| slidingWindowType   | COUNT_BASED      | Loại sliding window: COUNT_BASED hoặc TIME_BASED. |
| slidingWindowSize   | 100              | Kích thước sliding window. |
| minimumNumberOfCalls   | 100              | Số cuộc gọi tối thiểu cần có để tính tỷ lệ lỗi hoặc tỷ lệ cuộc gọi chậm. |
| waitDurationInOpenState   | 60000 [ms]       | Thời gian CircuitBreaker chờ trước khi chuyển từ OPEN sang HALF_OPEN. |
| automaticTransitionFromOpenToHalfOpenEnabled   | false            | Nếu true, CircuitBreaker tự động chuyển từ OPEN sang HALF_OPEN sau waitDurationInOpenState mà không cần có cuộc gọi kích hoạt. |
| recordExceptions   | empty            | Danh sách ngoại lệ được ghi nhận là lỗi. Nếu chỉ định, các ngoại lệ khác được tính là thành công (trừ khi bị ignore). |
| ignoreExceptions   | empty            | Danh sách ngoại lệ bị bỏ qua (không tính là lỗi hay thành công). |
| recordFailurePredicate   | throwable -> true | Predicate tùy chỉnh để xác định ngoại lệ nào được tính là lỗi. |
| ignoreExceptionPredicate   | throwable -> false | Predicate tùy chỉnh để xác định ngoại lệ nào bị bỏ qua. |

```java
// Create a custom configuration for a CircuitBreaker
// Ví dụ tạo cấu hình tùy chỉnh:
CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
  .failureRateThreshold(50)
  .slowCallRateThreshold(50)
  .waitDurationInOpenState(Duration.ofMillis(1000))
  .slowCallDurationThreshold(Duration.ofSeconds(2))
  .permittedNumberOfCallsInHalfOpenState(3)
  .minimumNumberOfCalls(10)
  .slidingWindowType(SlidingWindowType.TIME_BASED)
  .slidingWindowSize(5)
  .recordException(e -> INTERNAL_SERVER_ERROR
                 .equals(getResponse().getStatus()))
  .recordExceptions(IOException.class, TimeoutException.class)
  .ignoreExceptions(BusinessException.class, OtherBusinessException.class)
  .build();

// Create a CircuitBreakerRegistry with a custom global configuration
CircuitBreakerRegistry circuitBreakerRegistry = 
  CircuitBreakerRegistry.of(circuitBreakerConfig);

// Get or create a CircuitBreaker from the CircuitBreakerRegistry 
// with the global default configuration
CircuitBreaker circuitBreakerWithDefaultConfig = 
  circuitBreakerRegistry.circuitBreaker("name1");

// Get or create a CircuitBreaker from the CircuitBreakerRegistry 
// with a custom configuration
CircuitBreaker circuitBreakerWithCustomConfig = circuitBreakerRegistry
  .circuitBreaker("name2", circuitBreakerConfig);
```

Bạn có thể thêm các cấu hình chia sẻ giữa nhiều instance CircuitBreaker:

```java
CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
  .failureRateThreshold(70)
  .build();

circuitBreakerRegistry.addConfiguration("someSharedConfig", config);

CircuitBreaker circuitBreaker = circuitBreakerRegistry
  .circuitBreaker("name", "someSharedConfig");
```

Bạn có thể ghi đè (overwrite) cấu hình:

```java
CircuitBreakerConfig defaultConfig = circuitBreakerRegistry
   .getDefaultConfig();

CircuitBreakerConfig overwrittenConfig = CircuitBreakerConfig
  .from(defaultConfig)
  .waitDurationInOpenState(Duration.ofSeconds(20))
  .build();
```

Nếu bạn không muốn dùng CircuitBreakerRegistry để quản lý các instance, bạn cũng có thể tạo instance trực tiếp:

```java
// Create a custom configuration for a CircuitBreaker
CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
  .recordExceptions(IOException.class, TimeoutException.class)
  .ignoreExceptions(BusinessException.class, OtherBusinessException.class)
  .build();

CircuitBreaker customCircuitBreaker = CircuitBreaker
  .of("testName", circuitBreakerConfig);
```

Bạn cũng có thể tạo CircuitBreakerRegistry bằng các phương thức builder:

```java
Map <String, String> circuitBreakerTags = Map.of("key1", "value1", "key2", "value2");

CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.custom()
    .withCircuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
    .addRegistryEventConsumer(new RegistryEventConsumer() {
        @Override
        public void onEntryAddedEvent(EntryAddedEvent entryAddedEvent) {
            // implementation
        }
        @Override
        public void onEntryRemovedEvent(EntryRemovedEvent entryRemoveEvent) {
            // implementation
        }
        @Override
        public void onEntryReplacedEvent(EntryReplacedEvent entryReplacedEvent) {
            // implementation
        }
    })
    .withTags(circuitBreakerTags)
    .build();

CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testName");
```

Nếu bạn muốn cắm (plug in) triển khai Registry riêng, bạn có thể cung cấp một triển khai của interface RegistryStore và dùng phương thức builder:

```java
CircuitBreakerRegistry registry = CircuitBreakerRegistry.custom()
    .withRegistryStore(new YourRegistryStoreImplementation())
    .withCircuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
    .build();
```

## Decorate and execute a functional interface

Bạn có thể decorate bất kỳ Callable, Supplier, Runnable, Consumer, CheckedRunnable, CheckedSupplier, CheckedConsumer hoặc CompletionStage với một CircuitBreaker.
Bạn có thể gọi hàm đã được decorate bằng Try.of(…) hoặc Try.run(…) từ Vavr. Điều này cho phép xâu các hàm tiếp theo với map, flatMap, filter, recover hoặc andThen. Các hàm xâu chỉ được gọi nếu CircuitBreaker ở trạng thái CLOSED hoặc HALF_OPEN.

```java
// Given
CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults("testName");

// When I decorate my function
CheckedFunction0<String> decoratedSupplier = CircuitBreaker
        .decorateCheckedSupplier(circuitBreaker, () -> "This can be any method which returns: 'Hello");

// and chain an other function with map
Try<String> result = Try.of(decoratedSupplier)
                .map(value -> value + " world'");

// Then the Try Monad returns a Success<String>, if all functions ran successfully.
assertThat(result.isSuccess()).isTrue();
assertThat(result.get()).isEqualTo("This can be any method which returns: 'Hello world'");
```

## Consume emitted RegistryEvents

Bạn có thể đăng ký event consumer trên CircuitBreakerRegistry và thực hiện hành động khi một CircuitBreaker được tạo, thay thế hoặc xóa.

```java
CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
circuitBreakerRegistry.getEventPublisher()
  .onEntryAdded(entryAddedEvent -> {
    CircuitBreaker addedCircuitBreaker = entryAddedEvent.getAddedEntry();
    LOG.info("CircuitBreaker {} added", addedCircuitBreaker.getName());
  })
  .onEntryRemoved(entryRemovedEvent -> {
    CircuitBreaker removedCircuitBreaker = entryRemovedEvent.getRemovedEntry();
    LOG.info("CircuitBreaker {} removed", removedCircuitBreaker.getName());
  });
```

## Consume emitted CircuitBreakerEvents

Một CircuitBreakerEvent có thể là chuyển trạng thái, reset circuit breaker, một cuộc gọi thành công, một lỗi được ghi nhận hoặc một lỗi bị bỏ qua. Tất cả sự kiện chứa thông tin bổ sung như thời gian tạo sự kiện và thời lượng xử lý cuộc gọi. Nếu bạn muốn tiêu thụ các sự kiện, bạn phải đăng ký một event consumer.

```java
circuitBreaker.getEventPublisher()
    .onSuccess(event -> logger.info(...))
    .onError(event -> logger.info(...))
    .onIgnoredError(event -> logger.info(...))
    .onReset(event -> logger.info(...))
    .onStateTransition(event -> logger.info(...));
// Or if you want to register a consumer listening
// to all events, you can do:
circuitBreaker.getEventPublisher()
    .onEvent(event -> logger.info(...));
```

Bạn có thể dùng CircularEventConsumer để lưu các sự kiện trong một bộ đệm vòng với dung lượng cố định.

```java
CircularEventConsumer<CircuitBreakerEvent> ringBuffer = 
  new CircularEventConsumer<>(10);
circuitBreaker.getEventPublisher().onEvent(ringBuffer);
List<CircuitBreakerEvent> bufferedEvents = ringBuffer.getBufferedEvents()
```

Bạn có thể dùng adapter RxJava hoặc RxJava2 để chuyển EventPublisher thành một luồng phản ứng (Reactive Stream).

## Override the RegistryStore

Bạn có thể ghi đè RegistryStore mặc định trong bộ nhớ bằng một triển khai tùy chỉnh. Ví dụ, nếu bạn muốn dùng một Cache tự động loại bỏ các instance không dùng sau một thời gian.

```java
CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.custom()
  .withRegistryStore(new CacheCircuitBreakerRegistryStore())
  .build();
```