

# Getting Started

Một cách đơn giản để khởi tạo môi trường làm việc là tạo một dự án Spring 
qua https://start.spring.io/#!type=maven-project&dependencies=data-redis[start.spring.io] hoặc tạo một dự án Spring 
trong [https://spring.io/toolsSpring Tools].

## Examples Repository

Kho GitHub [https://github.com/spring-projects/spring-data-examplesspring-data-examples repository] chứa nhiều ví dụ 
mà bạn có thể tải về và thử nghiệm để làm quen với cách thư viện hoạt động.

## Hello World

Trước hết, bạn cần thiết lập một máy chủ Redis đang chạy. Spring Data Redis yêu cầu Redis phiên bản 2.6 trở lên và tích 
hợp với hai thư viện Java phổ biến cho Redis là Lettuce (https://github.com/lettuce-io/lettuce-core (github.com in Bing)) 
và Jedis (https://github.com/redis/jedis).

Bây giờ bạn có thể tạo một ứng dụng Java đơn giản để lưu và đọc một giá trị từ Redis.

Tạo ứng dụng chính để chạy, ví dụ như sau:


```java

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

public class RedisApplication {

	private static final Log LOG = LogFactory.getLog(RedisApplication.class);

	public static void main(String[] args) {

		LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory();
		connectionFactory.afterPropertiesSet();

		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setDefaultSerializer(StringRedisSerializer.UTF_8);
		template.afterPropertiesSet();

		template.opsForValue().set("foo", "bar");

		LOG.info("Value at foo:" + template.opsForValue().get("foo"));

		connectionFactory.destroy();
	}
}

```

```java

import reactor.core.publisher.Mono;

import java.time.Duration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

public class ReactiveRedisApplication {

	private static final Log LOG = LogFactory.getLog(ReactiveRedisApplication.class);

	public static void main(String[] args) {

		LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory();
		connectionFactory.afterPropertiesSet();

		ReactiveRedisTemplate<String, String> template = new ReactiveRedisTemplate<>(connectionFactory,
				RedisSerializationContext.string());

		Mono<Boolean> set = template.opsForValue().set("foo", "bar");
		set.block(Duration.ofSeconds(10));

		LOG.info("Value at foo:" + template.opsForValue().get("foo").block(Duration.ofSeconds(10)));

		connectionFactory.destroy();
	}
}

```

Ngay cả trong ví dụ đơn giản này, có vài điểm đáng chú ý:

Bạn có thể tạo một thể hiện của `RedisTemplate` (org.springframework.data.redis.core.RedisTemplate) (hoặc 
`ReactiveRedisTemplate` org.springframework.data.redis.core.ReactiveRedisTemplate cho sử dụng phản ứng) cùng với một 
`RedisConnectionFactory` (org.springframework.data.redis.connection.RedisConnectionFactory). Các connection factory là 
một lớp trừu tượng trên các driver được hỗ trợ.

Không có một cách duy nhất để sử dụng Redis vì Redis hỗ trợ nhiều cấu trúc dữ liệu khác nhau như keys (strings), lists, 
sets, sorted sets, streams, hashes, v.v.

# Drivers

Một trong những nhiệm vụ đầu tiên khi sử dụng Redis với Spring là kết nối tới kho dữ liệu thông qua IoC Container. Để 
làm điều đó, bạn cần connector (binding) Java. Dù chọn thư viện nào, bạn chỉ cần sử dụng một bộ API Spring Data Redis 
duy nhất (hành vi nhất quán giữa các connector). Gói org.springframework.data.redis.connection cùng các interface 
RedisConnection và RedisConnectionFactory là điểm khởi đầu để làm việc và lấy các kết nối đang hoạt động với Redis

## RedisConnection và RedisConnectionFactory

RedisConnection cung cấp khối xây dựng cốt lõi cho giao tiếp với Redis, vì nó xử lý việc trao đổi với backend Redis. 
Nó cũng tự động chuyển đổi các ngoại lệ của thư viện kết nối sang hệ thống ngoại lệ nhất quán của Spring (DAO exception 
hierarchy), giúp bạn có thể thay đổi connector mà không cần sửa code, vì ngữ nghĩa thao tác vẫn giữ nguyên.

Lưu ý: Khi cần truy cập API gốc của thư viện, RedisConnection cung cấp phương thức getNativeConnection() trả về đối 
tượng thô (native) dùng để giao tiếp.

Các đối tượng RedisConnection được tạo ra thông qua RedisConnectionFactory. Factory cũng đóng vai trò như 
PersistenceExceptionTranslator, tức là sau khi khai báo, nó cho phép chuyển đổi ngoại lệ một cách trong suốt (ví dụ qua 
@Repository và AOP). Xem phần tương ứng trong tài liệu Spring Framework để biết thêm chi tiết.

Quan trọng: Các lớp RedisConnection không an toàn cho đa luồng (not thread-safe). Mặc dù kết nối native (ví dụ Lettuce 
StatefulRedisConnection) có thể thread-safe, lớp LettuceConnection của Spring Data Redis thì không. Vì vậy không chia sẻ 
cùng một instance RedisConnection giữa nhiều thread — đặc biệt với các thao tác giao dịch hoặc blocking như BLPOP. 
Trong các thao tác transactional hoặc pipelining, RedisConnection giữ trạng thái mutable không được bảo vệ để hoàn thành 
thao tác, nên không an toàn khi dùng chung giữa nhiều thread. Đây là thiết kế có chủ ý.

Mẹo: Nếu bạn cần chia sẻ tài nguyên Redis stateful giữa nhiều thread vì lý do hiệu năng, hãy lấy kết nối native và dùng 
trực tiếp API của client Redis. Hoặc dùng RedisTemplate, vì RedisTemplate sẽ mượn và quản lý kết nối cho từng thao tác 
theo cách thread-safe. Xem tài liệu RedisTemplate để biết chi tiết.

Lưu ý thêm: Tùy cấu hình, factory có thể trả về kết nối mới hoặc kết nối đã tồn tại (khi dùng pool hoặc shared native connection).

Cách dễ nhất để làm việc với RedisConnectionFactory là cấu hình connector phù hợp trong IoC container và inject factory 
vào lớp sử dụng.

Không phải mọi connector đều hỗ trợ tất cả tính năng Redis. Nếu gọi một API Connection mà thư viện nền tảng không hỗ trợ, 
sẽ ném UnsupportedOperationException. Bảng dưới đây tóm tắt tính năng được hỗ trợ theo từng connector.

## Feature Availability across Redis Connectors


| Supported Feature	| Lettuce	| Jedis
|------------------------|------------------|--|
| Standalone Connections	| X	| X
| Master/Replica Connections	| X
| Redis Sentinel (Master Lookup, Sentinel Authentication, Replica Reads)	| Master Lookup, Sentinel Authentication, Replica Reads	| Master Lookup
| Redis Cluster (Cluster Connections, Cluster Node Connections, Replica Reads)	| Cluster Connections, Cluster Node Connections, Replica Reads	| Cluster Connections, Cluster Node Connections
| Transport Channels	| TCP, OS-native TCP (epoll, kqueue), Unix Domain Sockets	| TCP
| Connection Pooling	| X (using commons-pool2)	| X (using commons-pool2)
| Other Connection Features	| Singleton-connection sharing for non-blocking commands	| Pipelining and Transactions mutually exclusive
| SSL Support	| X	| X
| Pub/Sub	| X	| X
| Pipelining	| X	| X (Pipelining and Transactions mutually exclusive)
| Transactions	| X	| X (Pipelining and Transactions mutually exclusive)
| Datatype support	| Key, String, List, Set, Sorted Set, Hash, Server, Stream, Scripting, Geo, HyperLogLog	| Key, String, List, Set, Sorted Set, Hash, Server, Stream, Scripting, Geo, HyperLogLog
| Reactive (non-blocking) API	| X	

## Configuring the Lettuce Connector
Lettuce (github.com in Bing) là connector mã nguồn mở dựa trên Netty, được Spring Data Redis hỗ trợ qua gói org.springframework.data.redis.connection.lettuce.

Thêm dependency vào pom.xml:

```xml

<dependencies>

  <!-- other dependency elements omitted -->

  <dependency>
    <groupId>io.lettuce</groupId>
    <artifactId>lettuce-core</artifactId>
    <version>{lettuce}</version>
  </dependency>

</dependencies>


```
Ví dụ tạo LettuceConnectionFactory:

```java

@Configuration
class AppConfig {

  @Bean
  public LettuceConnectionFactory redisConnectionFactory() {

    return new LettuceConnectionFactory(new RedisStandaloneConfiguration("server", 6379));
  }
}


```

Một số tham số cấu hình Lettuce có thể điều chỉnh. Mặc định, tất cả LettuceConnection do LettuceConnectionFactory tạo chia sẻ cùng một kết nối native thread-safe cho các lệnh không blocking và không transactional. Nếu muốn dùng kết nối riêng cho mỗi lần, đặt shareNativeConnection = false. LettuceConnectionFactory cũng có thể cấu hình dùng LettucePool để pool các kết nối blocking/transactional hoặc tất cả kết nối khi shareNativeConnection = false.

Ví dụ cấu hình nâng cao (SSL, timeout) dùng LettuceClientConfigurationBuilder:

```java

@Bean
public LettuceConnectionFactory lettuceConnectionFactory() {

  LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
    .useSsl().and()
    .commandTimeout(Duration.ofSeconds(2))
    .shutdownTimeout(Duration.ZERO)
    .build();

  return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379), clientConfig);
}


```

Lettuce tích hợp với native transports của Netty, cho phép dùng Unix domain sockets để giao tiếp với Redis. Hãy chắc chắn thêm dependency native transport phù hợp với môi trường runtime. Ví dụ tạo LettuceConnectionFactory cho socket /var/run/redis.sock

```java

@Configuration
class AppConfig {

  @Bean
  public LettuceConnectionFactory redisConnectionFactory() {

    return new LettuceConnectionFactory(new RedisSocketConfiguration("/var/run/redis.sock"));
  }
}


```

Lưu ý: Netty hiện hỗ trợ epoll (Linux) và kqueue (BSD/macOS) cho OS-native transport.

Configuring the Jedis Connector
Jedis được Spring Data Redis hỗ trợ qua gói org.springframework.data.redis.connection.jedis.

Thêm dependency vào pom.xml:

```xml

<dependencies>

  <!-- other dependency elements omitted -->

  <dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>{jedis}</version>
  </dependency>

</dependencies>


```

Cấu hình Jedis đơn giản:

```java

@Configuration
class AppConfig {

  @Bean
  public JedisConnectionFactory redisConnectionFactory() {
    return new JedisConnectionFactory();
  }
}


```

Trong môi trường production, bạn có thể tinh chỉnh host, password, v.v. Ví dụ:

```java

@Configuration
class RedisConfiguration {

  @Bean
  public JedisConnectionFactory redisConnectionFactory() {

    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("server", 6379);
    return new JedisConnectionFactory(config);
  }
}


```
