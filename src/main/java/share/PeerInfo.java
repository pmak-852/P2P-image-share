package share;

import java.io.Serializable;
import java.util.*;

public class PeerInfo implements Serializable {
    private final String IP;
    private final String UserName;
    private final String pwd;

    private int requestListenPort;

    public PeerInfo(String IP, String username, String pwd, int requestListenPort) {
        this.IP = IP;
        this.requestListenPort = requestListenPort;
        this.UserName = username;
        this.pwd = pwd;
    }

    public String getUserName() {
        return UserName;
    }

    public String getIP() {
        return IP;
    }

    public int getRequestListenPort() {
        return requestListenPort;
    }

    public void setRequestListenPort(int port) {
        this.requestListenPort = port;
    }

    @Override
    public String toString() {
        return UserName;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeerInfo peerInfo = (PeerInfo) o;
        return (UserName.equals(peerInfo.UserName)) && (IP.equals(peerInfo.IP)) && (requestListenPort == peerInfo.requestListenPort);
    }
}
