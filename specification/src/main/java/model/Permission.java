package model;

import java.util.ArrayList;

public class Permission
{
    public static final int permissionCount = 7;

    private boolean createFolder;
    private boolean deleteFolder;
    private boolean uploadFolder;
    private boolean createFile;
    private boolean deleteFile;
    private boolean uploadFile;
    private boolean download;

    public Permission()
    {
        createFolder = false;
        deleteFolder = false;
        uploadFolder = false;
        createFile = false;
        deleteFile = false;
        uploadFile = false;
        download = false;
    }

    public ArrayList<Boolean> getPermissions()
    {
        ArrayList<Boolean> list = new ArrayList<>();
        list.add(createFolder);
        list.add(deleteFolder);
        list.add(uploadFolder);
        list.add(createFile);
        list.add(deleteFile);
        list.add(uploadFile);
        list.add(download);
        return list;
    }

    public void setPermissions(ArrayList<Boolean> permissions)
    {
        createFolder = permissions.get(0);
        deleteFolder = permissions.get(1);
        uploadFolder = permissions.get(2);
        createFile = permissions.get(3);
        deleteFile = permissions.get(4);
        uploadFile = permissions.get(5);
        download = permissions.get(6);
    }

    public boolean isCreateFolder()
    {
        return createFolder;
    }

    public void setCreateFolder(boolean createFolder)
    {
        this.createFolder = createFolder;
    }

    public boolean isDeleteFolder()
    {
        return deleteFolder;
    }

    public void setDeleteFolder(boolean deleteFolder)
    {
        this.deleteFolder = deleteFolder;
    }

    public boolean isUploadFolder()
    {
        return uploadFolder;
    }

    public void setUploadFolder(boolean uploadFolder)
    {
        this.uploadFolder = uploadFolder;
    }

    public boolean isCreateFile()
    {
        return createFile;
    }

    public void setCreateFile(boolean createFile)
    {
        this.createFile = createFile;
    }

    public boolean isDeleteFile()
    {
        return deleteFile;
    }

    public void setDeleteFile(boolean deleteFile)
    {
        this.deleteFile = deleteFile;
    }

    public boolean isUploadFile()
    {
        return uploadFile;
    }

    public void setUploadFile(boolean uploadFile)
    {
        this.uploadFile = uploadFile;
    }

    public boolean isDownload()
    {
        return download;
    }

    public void setDownload(boolean download)
    {
        this.download = download;
    }

    @Override
    public String toString()
    {
        String str = "";
        str += createFolder + ",";
        str += deleteFolder + ",";
        str += uploadFolder + ",";
        str += createFile + ",";
        str += deleteFile + ",";
        str += uploadFile + ",";
        str += download;
        return str;
    }
}
