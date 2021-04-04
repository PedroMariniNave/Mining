package com.zpedroo.mining.managers;

import com.zpedroo.mining.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;

public class FileManager {

    private FileConfiguration language;
    private File pdfile;
    private String name;

    public FileManager(String path, String name, String resource) {
        this(path, name, resource, null);
    }

    public FileManager(String path, String name, String resource, String defaultResource) {
        this.name = name;
        this.pdfile = new File(Main.get().getDataFolder() + path, name + ".yml");

        if (!pdfile.exists()) {
            try {
                pdfile.getParentFile().mkdirs();
                pdfile.createNewFile();

                if (resource != null) {
                    InputStream is = Main.get().getResource(resource + ".yml");

                    if ((is == null) && (defaultResource != null)) {
                        copy(Main.get().getResource(defaultResource + ".yml"), pdfile);
                    } else {
                        copy(is, pdfile);
                    }
                }
            } catch (IOException ex) {
                System.out.println("Could not create " + name + ".yml!");
            }
        }

        BufferedReader in;

        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(pdfile), "UTF-8"));
            language = YamlConfiguration.loadConfiguration(in);

            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public FileConfiguration get() {
        return language;
    }

    public void save() {
        try {
            language.save(pdfile);
        } catch (IOException ex) {
            System.out.println("Could not save " + this.name + ".yml!");
        }
    }

    public void reload() {
        try {
            language = YamlConfiguration.loadConfiguration(pdfile);
        } catch (Exception ex) {
            System.out.println("Could not reload " + this.name + ".yml!");
        }
    }

    public static void copy(InputStream is, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len=is.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            is.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}