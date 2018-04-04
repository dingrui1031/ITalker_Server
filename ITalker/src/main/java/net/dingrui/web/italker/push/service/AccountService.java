package net.dingrui.web.italker.push.service;

import net.dingrui.web.italker.push.bean.api.account.RegisterModel;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author dingrui
 */
@Path("/account")
public class AccountService {

    @POST
    //指定请求与返回的响应体
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/register")
    public RegisterModel register(RegisterModel registerModel){
//        User user = new User();
//        user.setName(registerModel.getName());
//        user.setSex(0);
        return registerModel;
    }
}
