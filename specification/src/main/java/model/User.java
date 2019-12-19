package model;

public class User
{
    private static User user = null;
    private boolean admin;
    private Permission permission;
    private String username;

    private User()
    {
        permission = new Permission();
        username = "";
        this.admin = false;
    }

    public void init(String username, Permission permission)
    {
        this.username = username;
        this.permission = permission;
        this.admin = false;
    }

    public static User getInstance()
    {
        if (user == null)
            user = new User();
        return user;
    }

    public static boolean isCreated()
    {
        if (user == null)
            return false;
        return true;
    }

    public static void destroyUser()
    {
        user = null;
    }

    public Permission getPermission()
    {
        return permission;
    }

    public void setPermission(Permission permission)
    {
        this.permission = permission;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public boolean isAdmin()
    {
        return admin;
    }

    public void setAdmin(boolean admin)
    {
        this.admin = admin;
    }
}
