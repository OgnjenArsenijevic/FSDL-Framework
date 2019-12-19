package model;

import lombok.Data;
import lombok.ToString;

@Data  @ToString
public class FileMetadata
{

    private String name;

    private String path;

    public FileMetadata(String name, String pathLower)
    {
        this.name = name;
        this.path = pathLower;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}
