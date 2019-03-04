package xyz.sprov.blog.sprovui.service;

import xyz.sprov.blog.sprovui.exception.V2rayException;
import xyz.sprov.blog.sprovui.util.ExecUtil;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;

//@Service
public class V2rayService {

//    @Value("${spring.profiles.active}")
//    private String active;

//    @Value("${v2ray.install}")
    private String installCmd = "bash <(curl -L -s https://install.direct/go.sh)";

//    @Value("${v2ray.update}")
    private String updateCmd = "bash <(curl -L -s https://install.direct/go.sh) -f";

    private String startCmd = "systemctl start v2ray";
    private String restartCmd = "systemctl restart v2ray";
    private String stopCmd = "systemctl stop v2ray";

    private final AtomicBoolean inOperation = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
//        if (active.equals("prod") && !SystemUtils.IS_OS_LINUX) {
//            System.err.println("本程序只支持Linux系统");
//            System.exit(-1);
//        }
    }

    /**
     * 是否已安装
     */
    public boolean isInstalled() throws IOException, InterruptedException {
        int status = status();
        return status == 0 || status == 1;
    }

    /**
     * 是否正在运行
     */
    public boolean isRunning() throws IOException, InterruptedException {
        return status() == 0;
    }

    /**
     * v2ray状态
     * 0：运行
     * 1：未运行
     * 2：未安装
     */
    public int status() throws IOException, InterruptedException {
        String result = ExecUtil.execForResult("sh", "-c", "systemctl status v2ray  | grep Active | awk '{print $3}' | cut -d \"(\" -f2 | cut -d \")\" -f1");
        if (result.contains("could not be found.")) {
            return 2;
        } else if (result.contains("running")) {
            return 0;
        } else {
            return 1;
        }
//        result = ExecUtil.execForResult("sh", "-c", "ps -ef | grep v2ray | grep -v grep | awk '{print $2}'");
//        if (result.length() > 0) {
//            return 0;
//        } else {
//            return 1;
//        }
    }

    /**
     * 安装v2ray
     */
    public boolean install() throws IOException, InterruptedException {
        return install(false);
    }

    /**
     * 安装v2ray
     */
    private boolean install(boolean forceUpdate) throws IOException, InterruptedException {
        if (inOperation.get()) {
            throw new V2rayException("正在操作中");
        } else if (!forceUpdate && isInstalled()) {
            throw new V2rayException("请不要重复安装");
        }
        return operation(forceUpdate ? updateCmd : installCmd);
    }

    /**
     * 更新v2ray
     */
    public boolean update() throws IOException, InterruptedException {
        return install(true);
    }

    /**
     * 卸载v2ray
     *
     * 暂时不写
     */
    public boolean uninstall() throws IOException, InterruptedException {
        if (inOperation.get()) {
            throw new V2rayException("正在操作中");
        } else if (!isInstalled()) {
            throw new V2rayException("没有安装v2ray");
        }
        synchronized (inOperation) {
            ExecUtil.execForStatus(stopCmd);
            ExecUtil.execForStatus("systemctl disable v2ray");
        }
        return false;
    }

    /**
     * 启动v2ray
     */
    public boolean start() throws IOException, InterruptedException {
        if (inOperation.get()) {
            throw new V2rayException("正在操作中，请稍后");
        }
        return operation(startCmd);
    }

    /**
     * 重启v2ray
     */
    public boolean restart() throws IOException, InterruptedException {
        if (inOperation.get()) {
            throw new V2rayException("正在操作中，请稍后");
        }
        return operation(restartCmd);
    }

    /**
     * 关闭v2ray
     */
    public boolean stop() throws IOException, InterruptedException {
        if (inOperation.get()) {
            throw new V2rayException("正在操作中，请稍后");
        }
        return operation(stopCmd);
    }

    private boolean operation(String cmd) throws IOException, InterruptedException {
        synchronized (inOperation) {
            try {
                inOperation.set(true);
                return ExecUtil.execForStatus(cmd) == 0;
            } finally {
                inOperation.set(false);
            }
        }
    }

}
