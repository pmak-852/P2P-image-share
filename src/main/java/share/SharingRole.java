package share;

import share.data.ImagePanel;
import share.message.P2PMessage;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class SharingRole {
    /* Maintain the sharing and request list */
    protected final List<PeerInfo> availablePeer = Collections.synchronizedList(new ArrayList<>());
    protected final ImagePanel panel = new ImagePanel(30, 20, 20);
    protected PeerInfo self;

//    protected final ClearThread cleaner = new ClearThread();

    public SharingRole(PeerInfo self) {
        this.self = self;
    }

    public void printPeers() {
        System.out.println("=== Current Peers ===");
        for (PeerInfo peer : availablePeer) {
            System.out.println(peer.getUserName());
        }
        System.out.println("=== END ===");
    }

    protected PeerInfo getNextPeer() {
        if (availablePeer.size() > 0) {
            return availablePeer.remove(0);
        }
        return null;
    }

    protected void mergePeers(P2PMessage res) {
        if (!this.availablePeer.contains(res.getSender())) {
            this.availablePeer.add(res.getSender());
        }
        if (res.getPeers() != null) {
            res.getPeers().forEach(peerInfo -> {
                if (!this.availablePeer.contains(peerInfo) && !peerInfo.equals(this.self)) {
                    this.availablePeer.add(peerInfo);
                }
            });
        }
    }

    protected boolean containsPeer(PeerInfo p) {
        return availablePeer.contains(p);
    }

//    class ClearThread extends Thread {
//        @Override
//        public void run() {
//            while (true) {
//                try {
//                    currentRequest.forEach(req -> {
//                        if (req.getState() == State.TERMINATED) {
//                            currentRequest.remove(req);
//                            System.out.println("Delete thread between " + req.response.getSender() + " " + req.response.getReceiver());
//                        }
//                    });
//                    sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
}
