package net.dingrui.web.italker.push.factory;

import com.google.common.base.Strings;
import net.dingrui.web.italker.push.bean.db.User;
import net.dingrui.web.italker.push.utils.Hib;
import net.dingrui.web.italker.push.utils.TextUtil;

import java.util.List;
import java.util.UUID;

/**
 * @author dingrui
 */
public class UserFactory {

    /**
     * 通过token找到user
     * token的信息只能自己使用，查询的是自己的信息
     *
     * @param token 用户的token
     * @return
     */
    public static User findByToken(String token) {
        return Hib.query(session -> (User) session
                .createQuery("from User where token=:inToken")
                .setParameter("inToken", token)
                .uniqueResult());
    }

    /**
     * 通过phone找到user
     *
     * @param phone
     * @return
     */
    public static User findByPhone(String phone) {
        return Hib.query(session -> (User) session
                .createQuery("from User where phone=:inPhone")
                .setParameter("inPhone", phone)
                .uniqueResult());
    }

    /**
     * 通过name找到user
     *
     * @param name
     * @return
     */
    public static User findByName(String name) {
        return Hib.query(session -> (User) session
                .createQuery("from User where name=:inName")
                .setParameter("inName", name)
                .uniqueResult());
    }

    /**
     * 更新用户信息到数据库
     * @param user
     * @return
     */
    public static User update(User user) {
        return Hib.query(session -> {
            session.saveOrUpdate(user);
            return user;
        });
    }


    /**
     * 绑定用户的pushId
     *
     * @param user   自己的user
     * @param pushId 自己设备的pushId
     * @return
     */
    public static User bindPushId(User user, String pushId) {

        if (Strings.isNullOrEmpty("pushId"))
            return null;

        //首先查找是否有其他账户绑定该pushId
        //取消绑定，避免推送混乱
        //查询的列表不能包括自己
        Hib.queryOnly(session -> {
            List<User> userList = (List<User>) session
                    .createQuery("from User where lower(pushId)=:pushId and id!=:userId")
                    .setParameter("pushId", pushId.toLowerCase())
                    .setParameter("userId", user.getId())
                    .list();

            for (User u : userList) {
                //更新为null
                u.setPushId(null);
                session.saveOrUpdate(u);
            }
        });

        if (pushId.equalsIgnoreCase(user.getPushId())) {
            //如果当前需要绑定设备id,之前已经绑定过了，那么不需要绑定
            return user;
        } else {
            //如果当前需要绑定的设备id，和需要绑定的不一样，那么需要单点登录，让之前的设备退出账户
            //给之前的设备推送一条退出消息
            if (Strings.isNullOrEmpty(user.getPushId())) {
                // todo 推送一条退出消息

            }

            //更新用户的设备id
            user.setPushId(pushId);
            return update(user);
        }
    }

    /**
     * 使用账号密码登录
     *
     * @param account
     * @param password
     * @return
     */
    public static User login(String account, String password) {
        String accountStr = account.trim();
        //将原文密码进行相同的处理，才能匹配加密后的
        String encodePassword = encodePassword(password);

        //查找数据库
        User user = Hib.query(session ->
                (User) session.createQuery("from User where phone=:phone and password=:password")
                        .setParameter("phone", accountStr)
                        .setParameter("password", encodePassword)
                        .uniqueResult());

        if (user != null) {
            //对用户进行登录操作，更新token
            user = login(user);
        }

        return user;
    }

    /**
     * 用户注册
     * 注册的操作需要写入数据库，并返回数据库中的User信息
     *
     * @param account  账号
     * @param password 密码
     * @param name     用户名
     * @return
     */
    public static User register(String account, String password, String name) {
        //取出账户首位空格
        account = account.trim();
        //处理密码，进行加密
        password = encodePassword(password);

        User user = createUser(account, password, name);

        if (user != null) {
            // 注册成功就直接登录
            user = login(user);
        }
        return user;
    }

    /**
     * 注册部分的新建用户逻辑
     *
     * @param account  手机号
     * @param password 加密后的密码
     * @param name     用户名
     * @return 返回一个用户
     */
    public static User createUser(String account, String password, String name) {
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        // 账户就是手机号
        user.setPhone(account);
        // 数据库存储
        return Hib.query(session -> (User) session.save(user));
    }

    /**
     * 对一个user进行登录操作
     * 实际上就是对token进行操作
     *
     * @param user
     * @return
     */
    public static User login(User user) {
        //使用一个随机的UUID作为新的token
        String newToken = UUID.randomUUID().toString();
        //对新token进行64加密
        newToken = TextUtil.encodeBase64(newToken);
        user.setToken(newToken);

        return update(user);
    }

    /**
     * 对密码进行加密操作
     *
     * @param password 原文
     * @return 密文
     */
    public static String encodePassword(String password) {
        //对密码去除首位空格
        password = password.trim();
        //进行非对称的md5加密，加盐会更安全，盐也需要存储
        password = TextUtil.getMD5(password);
        //在进行一次base64对称加密，可以采取加盐的方式
        return TextUtil.encodeBase64(password);
    }
}
