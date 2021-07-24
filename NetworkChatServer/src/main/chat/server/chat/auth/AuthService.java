package main.chat.server.chat.auth;

import java.util.HashSet;
import java.util.Set;

public class AuthService {
    private static final Set<User> USERS = new HashSet<>();

    //    Инициализируем сет в блоке инициализации (в Java8 нет set.of)
    static {
        USERS.add(new User("login1", "pass1", "username1"));
        USERS.add(new User("login2", "pass2", "username2"));
        USERS.add(new User("login3", "pass3", "username3"));
    }


    public String getUsernameByLoginAndPassword(String login, String password) {
//        Создаем новый экземпляр класса с переданным логином и паролем. На основе сравнения этого
//        созданного объекта с объектами из сета будем определять, есть ли такой юзер и,
//        если есть, возвращать его username.
        User requiredUser = new User(login, password);

        for (User user : USERS) {
//            сравнение идет по логину и паролю: для этого в классе User переопределили
//            методы equals() и hashCode(): заложили в них только поля login и password
            if (requiredUser.equals(user)) {
                return user.getUsername();
            }
        }

        return null;
    }
}
