package data;

import java.util.ArrayList;

public class Permission {
    public static final int permissionCount = 8;

    private boolean createFolder;
    private boolean deleteFolder;
    private boolean uploadFolder;
    private boolean uploadFile;
    private boolean deleteFile;
    private boolean download;
    private boolean createUser;
    private boolean deleteUser;

    public Permission() {
        createFolder = false;
        deleteFolder = false;
        uploadFolder = false;
        uploadFile = false;
        deleteFile = false;
        download = false;
        createUser = false;
        deleteUser = false;
    }

    public ArrayList<Boolean> getPermissions() {
        ArrayList<Boolean> list = new ArrayList<>();
        list.add(createFolder);
        list.add(deleteFolder);
        list.add(uploadFolder);
        list.add(uploadFile);
        list.add(deleteFile);
        list.add(download);
        list.add(createUser);
        list.add(deleteUser);
        return list;
    }

    public void setPermissions(ArrayList<Boolean> permissions) {
        createFolder = permissions.get(0);
        deleteFolder = permissions.get(1);
        uploadFolder = permissions.get(2);
        uploadFile = permissions.get(3);
        deleteFile = permissions.get(4);
        download = permissions.get(5);
        createUser = permissions.get(6);
        deleteUser = permissions.get(7);
    }

    public boolean isCreateFolder() {
        return createFolder;
    }

    public void setCreateFolder(boolean createFolder) {
        this.createFolder = createFolder;
    }

    public boolean isDeleteFolder() {
        return deleteFolder;
    }

    public void setDeleteFolder(boolean deleteFolder) {
        this.deleteFolder = deleteFolder;
    }

    public boolean isUploadFolder() {
        return uploadFolder;
    }

    public void setUploadFolder(boolean uploadFolder) {
        this.uploadFolder = uploadFolder;
    }

    public boolean isUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(boolean uploadFile) {
        this.uploadFile = uploadFile;
    }

    public boolean isDeleteFile() {
        return deleteFile;
    }

    public void setDeleteFile(boolean deleteFile) {
        this.deleteFile = deleteFile;
    }

    public boolean isDownload() {
        return download;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    public boolean isCreateUser() {
        return createUser;
    }

    public void setCreateUser(boolean createUser) {
        this.createUser = createUser;
    }

    public boolean isDeleteUser() {
        return deleteUser;
    }

    public void setDeleteUser(boolean deleteUser) {
        this.deleteUser = deleteUser;
    }

    @Override
    public String toString() {
        String str = "";
        str += createFolder + ",";
        str += deleteFolder + ",";
        str += uploadFolder + ",";
        str += uploadFile + ",";
        str += deleteFile + ",";
        str += download + ",";
        str += createUser + ",";
        str += deleteUser;
        return str;
    }
}
