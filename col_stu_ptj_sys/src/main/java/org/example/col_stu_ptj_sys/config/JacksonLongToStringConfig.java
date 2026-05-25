package org.example.col_stu_ptj_sys.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 避免前端 JavaScript 解析 Long 精度丢失（雪花ID > 2^53-1）。
 */
@Configuration
public class JacksonLongToStringConfig {

    @Bean
    public Module jacksonLongToStringModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        return module;
    }
}