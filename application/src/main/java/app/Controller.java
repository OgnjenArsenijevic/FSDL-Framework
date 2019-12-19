package app;

import data.Permission;
import data.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Controller {

    public Permission getPermissionForUser(String username) {
        Permission permission = new Permission();

        Scanner sc;
        //File file = new File("src/main/resources/userPermissions.txt");
        File file = new File("C:/resources/userPermissions.txt");
        try {
            sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String curr = sc.nextLine();
                String[] split = curr.split("///");
                if (split[0].equals(username)) {
                    ArrayList<Boolean> permissions = new ArrayList<>();
                    String[] splitPer = split[1].split(",");
                    for (String str : splitPer) {
                        if (str.equals("true")) {
                            permissions.add(true);
                        } else if (str.equals("false")) {
                            permissions.add(false);
                        }
                    }
                    permission.setPermissions(permissions);
                    return permission;
                }
            }
            sc.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
        System.out.println("User doesn't exist in database");
        return null;
    }

    public boolean register(String username, String password) {
        if (!checkUser(username, password))
            return false;

        Scanner sc;
        //File file = new File("src/main/resources/userData.txt");
        File file = new File("C:/resources/userData.txt");

        try {
            sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String curr = sc.nextLine();
                String[] split = curr.split("///");
                if (split[0].equals(username)) {
                    System.out.println("Username already exists");
                    return false;
                }
            }
            sc.close();
        } catch (Exception err) {
            err.printStackTrace();
            return false;
        }

        try {
            String user = username + "///" + getHashed(password, "SHA-512");
            FileWriter fw = new FileWriter(file, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(user);
            writePermissionForUser(username);
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        String newLine = username;
        newLine += "///";
        newLine += getHashed(newPassword, "SHA-512");
        String oldLine = username;
        oldLine += "///";
        oldLine += getHashed(oldPassword, "SHA-512");
        if (replaceLine(new File("src/main/resources/userData.txt"), oldLine, newLine))
            return true;
        System.out.println("You entered wrong password");
        return false;
    }

    public boolean changePermission(String username, ArrayList<Boolean> permissions) {
        if (username.equals("admin")) {
            System.out.println("Permissions for admin cannot be changed");
            return false;
        }
        String oldLine = username;
        oldLine += "///";
        oldLine += getPermissionForUser(username).toString();
        String newLine = username;
        newLine += "///";
        Permission p = new Permission();
        p.setPermissions(permissions);
        newLine += p.toString();
        if (replaceLine(new File("src/main/resources/userPermissions.txt"), oldLine, newLine))
            return true;
        System.out.println("User doesn't exist in database");
        return false;
    }

    private boolean replaceLine(File file, String oldLine, String newLine) {
        try {
            List<String> fileContent = new ArrayList<>(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
            for (int i = 0; i < fileContent.size(); i++) {
                if (fileContent.get(i).equals(oldLine)) {
                    fileContent.set(i, newLine);
                    Files.write(file.toPath(), fileContent, StandardCharsets.UTF_8);
                    return true;
                }
            }
        } catch (IOException err) {
            err.printStackTrace();
        }
        return false;
    }

    private boolean

            +++++++++++++(String username) {
        File file = new File("src/main/resources/userPermissions.txt");
        try {
            String user = username + "///";
            for (int i = 0; i < Permission.permissionCount - 1; i++) {
                user += "false,";
            }
            user += "false";
            FileWriter fw = new FileWriter(file, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(user);
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean login(String username, String password) {
        if (!checkUser(username, password))
            return false;

        Scanner sc;
        //File file = new File("src/main/resources/userData.txt");
        File file = new File("C:/resources/userData.txt");

        try {
            sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String curr = sc.nextLine();
                String[] split = curr.split("///");
                if (split[0].equals(username)) {
                    if (split[1].equals(getHashed(password, "SHA-512")))
                        return true;
                    else {
                        System.out.println("You entered the wrong password");
                        return false;
                    }
                }
            }
            sc.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
        System.out.println("Username not found");
        return false;
    }

    public boolean logout() {
        if (User.isCreated()) {
            User.destroyUser();
            return true;
        }
        System.out.println("You are already logged out");
        return false;
    }

    private String getHashed(String pass, String lvl) {
        String generatedPassword = "";
        try {
            StringBuilder sb = new StringBuilder();
            MessageDigest md = MessageDigest.getInstance(lvl);
            byte[] byteArray = md.digest(pass.getBytes());

            for (int i = 0; i < byteArray.length; i++) {
                sb.append(Integer.toString((byteArray[i] & 0xff) + 0x100, 16).substring(1));
            }

            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    private boolean checkUser(String username, String password) {
        boolean check = true;
        if (username.equals("")) {
            System.out.println("Please enter username");
            check = false;
        }
        if (password.equals("")) {
            System.out.println("Please enter password");
            check = false;
        }
        if (username.contains(" ")) {
            System.out.println("Username cannot contain spaces");
            check = false;
        }
        if (username.contains("/")) {
            System.out.println("Username cannot contain /");
            check = false;
        }
        return check;
    }
}
