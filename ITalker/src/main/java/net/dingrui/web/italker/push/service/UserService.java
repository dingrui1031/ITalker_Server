package net.dingrui.web.italker.push.service;

import com.google.common.base.Strings;
import net.dingrui.web.italker.push.bean.api.base.ResponseModel;
import net.dingrui.web.italker.push.bean.api.user.UpdateInfoModel;
import net.dingrui.web.italker.push.bean.card.UserCard;
import net.dingrui.web.italker.push.bean.db.User;
import net.dingrui.web.italker.push.bean.db.UserFollow;
import net.dingrui.web.italker.push.factory.UserFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户信息处理的Service
 *
 * @author dingrui
 */
// 127.0.0.1/api/user/...
@Path("/user")
public class UserService extends BaseService {

    //用户更新
    @PUT
    @Path("/update") //127.0.0.1/api/user 不需要写，就是当前目录
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

    // 拉取联系人
    @GET
    @Path("/contact")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<UserCard>> contact() {
        User self = getSelf();

        //拿到我的联系人
        List<User> users = UserFactory.contacts(self);

        //转换为usercard
        List<UserCard> userCards = users.stream()
                .map(user -> {
                    return new UserCard(user, true);
                })//map操作，相当于转置操作，user->usercard
                .collect(Collectors.toList());
        return ResponseModel.buildOk(userCards);
    }

    // 关注人 简化：其实是双方同时关注
    @PUT //修改类使用put
    @Path("/follow/{followId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> follow(@PathParam("followId") String followId) {
        User self = getSelf();

        //不能关注自己
        if (self.getId().equalsIgnoreCase(followId)
                || Strings.isNullOrEmpty(followId))
            //返回参数异常
            return ResponseModel.buildParameterError();

        User followUser = UserFactory.findById(followId);
        //没有这个人
        if (followUser == null)
            //未找到人
            return ResponseModel.buildNotFoundUserError(null);

        //备注默认没有，后面可以扩展
        followUser = UserFactory.follow(self, followUser, null);
        if (followUser == null)
            //关注失败，返回服务器异常
            return ResponseModel.buildServiceError();

        //todo 通知我关注的人，我关注了他

        //返回关注的人的信息
        return ResponseModel.buildOk(new UserCard(followUser, true));

    }

    //获取某人的信息
    @GET
    @Path("{id}")// 127.0.0.1/api/user/{id}
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<UserCard> getUser(@PathParam("id") String id) {
        if (Strings.isNullOrEmpty(id))
            //返回参数异常
            return ResponseModel.buildParameterError();

        User self = getSelf();
        if (self.getId().equalsIgnoreCase(id))
            //返回自己，不必查询数据库
            return ResponseModel.buildOk(new UserCard(self, true));

        User user = UserFactory.findById(id);
        if (user == null)
            //没找到用户
            return ResponseModel.buildNotFoundUserError(null);

        //如果我们之间有关注的记录，则我已关注需要查询信息的用户
        boolean isFollow = UserFactory.getUserFollow(self, user) != null;
        return ResponseModel.buildOk(new UserCard(user, isFollow));
    }


    //搜索人的接口实现
    //为了简化分页，只返回20条数据
    @GET //搜索人不涉及数据更改
    //127.0.0.1/api/user/search
    @Path("/search/{name:(.*)?}") //名字为任意字符，可以为空
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseModel<List<UserCard>> search(@DefaultValue("") @PathParam("name") String name) {
        User self = getSelf();

        //先查询数据
        List<User> searchUsers = UserFactory.search(name);
        //把查询的人封装为UserCard
        //判断这些人是否有我已经关注的人
        //如果有，则返回的关注状态中应该已经设置好状态

        //拿出我的联系人
        List<User> contacts = UserFactory.contacts(self);

        List<UserCard> userCards = searchUsers.stream()
                .map(user -> {
                    //判断这个人是否在我的联系人中
                    boolean isFollow = user.getId().equalsIgnoreCase(self.getId())
                            //进行联系人的任意匹配
                            || contacts.stream().anyMatch(
                            contactUser -> contactUser.getId()
                                    .equalsIgnoreCase(user.getId())
                    );
                    return new UserCard(user, isFollow);
                }).collect(Collectors.toList());

        //返回
        return ResponseModel.buildOk(userCards);
    }
}
