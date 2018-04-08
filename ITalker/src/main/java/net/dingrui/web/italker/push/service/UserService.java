package net.dingrui.web.italker.push.service;

import com.google.common.base.Strings;
import net.dingrui.web.italker.push.bean.api.base.ResponseModel;
import net.dingrui.web.italker.push.bean.api.user.UpdateInfoModel;
import net.dingrui.web.italker.push.bean.card.UserCard;
import net.dingrui.web.italker.push.bean.db.User;
import net.dingrui.web.italker.push.factory.UserFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 用户信息处理的Service
 *
 * @author dingrui
 */
// 127.0.0.1/api/user/...
@Path("/user")
public class UserService {

    @PUT
    //@Path("") //127.0.0.1/api/user 不需要写，就是当前目录
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> update(@HeaderParam("token") String token,
                                          UpdateInfoModel updateInfoModel) {

        if (Strings.isNullOrEmpty("token") || !UpdateInfoModel.check(updateInfoModel)) {
            return ResponseModel.buildParameterError();
        }

        //拿到自己个人信息
        User user = UserFactory.findByToken("token");

        if (user!=null) {
            user = updateInfoModel.updateToUser(user);
            user = UserFactory.update(user);
            UserCard userCard = new UserCard(user, true);
            return ResponseModel.buildOk(userCard);
        }else {
            //token失效，所以无法进行绑定
            return ResponseModel.buildAccountError();
        }

    }
}
