package share;

import org.junit.jupiter.api.Test;
import share.message.P2PMessage;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class ImageClientTest {
    @Test
    void name() throws IOException, InterruptedException {
        PeerInfo test = new PeerInfo("localhost", "UnitDebug", null, 0);
        PeerInfo target = new PeerInfo("localhost", "ImageClient", null, 8002);
        P2PMessage reqImgBlk = P2PMessage.reqImageBlock(test, target, null, "testImage.png", 0);
        RequestAbstract req = new RequestAbstract(reqImgBlk) {
            @Override
            public void handleResponse(P2PMessage res) {
                return;
            }

        };
        req.start();
        req.join();
    }

    @Test
    void changeImage() throws IOException, InterruptedException {
        PeerInfo test = new PeerInfo("localhost", "UnitDebug", null, 0);
        PeerInfo target = new PeerInfo("localhost", "ImageClient", null, 8002);
        P2PMessage reqImgBlk = P2PMessage.reqImageUpdate(test, target, null, "testImage2.png");
        RequestAbstract req = new RequestAbstract(reqImgBlk) {
            @Override
            public void handleResponse(P2PMessage res) {
                return;
            }
        };
        req.start();
        req.join();
    }

    @Test
    void multiClients() {
//        PeerInfo c1 = new PeerInfo("localhost", "ImageClient1", null, 8002);
        PeerInfo c2 = new PeerInfo("localhost", "ImageClient2", null, 8003);
        PeerInfo c3 = new PeerInfo("localhost", "ImageClient3", null, 8004);
        PeerInfo c4 = new PeerInfo("localhost", "ImageClient4", null, 8005);

//        ImageClient ic1 = new ImageClient(c1, "testImage.png");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ImageClient ic2 = new ImageClient(c2, "testImage.png");

        while (true) {

        }
    }
}