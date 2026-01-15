package owo.pigeon.config;

import owo.pigeon.Pigeonqwq;

import java.io.File;

public abstract class Config {
    protected final String fileName;

    public Config(String fileName) {
        this.fileName = fileName;
    }

    /** 子类可覆盖：决定放在哪个子目录 */
    protected File getBaseDir() {
        return new File("config/" + Pigeonqwq.MOD_ID);
    }

    /** 最终文件路径 */
    public File getFile() {
        File dir = getBaseDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return new File(dir, fileName);
    }

    public abstract void load();
    public abstract void save();

    public boolean exists() {
        return getFile().exists();
    }
}
