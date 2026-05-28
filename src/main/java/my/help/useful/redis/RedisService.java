package my.help.useful.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public void saveData(Integer key, String value) {
        redisTemplate.opsForValue().set(String.valueOf(key), value);
    }

    public String getValue(Integer key) {
        return redisTemplate.opsForValue().get(String.valueOf(key));
    }

    public Integer getTotalKeysCount() {
        int size = Math.toIntExact(redisTemplate.getConnectionFactory()
                .getConnection()
                .dbSize());
        System.out.println("В редисе сейчас: " + size + " значений");

        return size-1;
    }
}
