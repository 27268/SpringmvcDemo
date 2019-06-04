package spring_security;

public class UserDetail {

    private String userName;
    private String password;

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



    public void setUserName(String userName){
        this.userName = userName;
    }
}
