package net.dingrui.web.italker.push.service;

import net.dingrui.web.italker.push.bean.db.User;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @author dingrui
 */
@Path("/account")
public class AccountService {

    @GET
    @Path("/login")
    public String get(){
        return "You get login!";
    }

    @POST
    //指定请求与返回的响应体
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/login")
    public User post(){
        User user = new User();
        user.setName("张三");
        user.setSex(0);
        return user;
    }
}
