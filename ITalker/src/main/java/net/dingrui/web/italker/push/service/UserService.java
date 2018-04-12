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
public class UserService extends BaseService {

    @PUT
    //@Path("") //127.0.0.1/api/user 不需要写，就是当前目录
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> update(UpdateInfoModel updateInfoModel) {

        if (!UpdateInfoModel.check(updateInfoModel)) {
            return ResponseModel.buildParameterError();
        }

        //拿到自己个人信息
//        User user = UserFactory.findByToken("token");
        User self = getSelf();

        self = updateInfoModel.updateToUser(self);
        // 更新用户信息
        self = UserFactory.update(self);
        // 构建自己的用户信息
        UserCard userCard = new UserCard(self, true);
        //返回
        return ResponseModel.buildOk(userCard);

    }
}
