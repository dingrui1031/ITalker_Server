package net.dingrui.web.italker.push;

import net.dingrui.web.italker.push.provider.GsonProvider;
import net.dingrui.web.italker.push.service.AccountService;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.logging.Logger;

/**
 * @author dingrui
 */
public class Application extends ResourceConfig {
    public Application(){
        //注册逻辑处理的包名
        register(AccountService.class.getPackage().getName());

        // 注册我们的全局请求拦截器
//        register(AuthRequestFilter.class);

        // 注册Json解析器
        // register(JacksonJsonProvider.class);
        // 替换解析器为Gson,JacksonJsonProvider会对集合全量输出，会把输出流沾满
        register(GsonProvider.class);

        //注册日志打印输出
        register(Logger.class);
    }
}
