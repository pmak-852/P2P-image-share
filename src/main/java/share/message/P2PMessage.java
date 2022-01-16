package share.message;

import share.PeerInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

public class P2PMessage implements Serializable {
    PeerInfo Sender, Receiver;
    P2PMessageType MessageType;
    String ImgName;
    int ImageBlockN;
    SerializedImage SerializedImg;
    List<PeerInfo> peers;


    private P2PMessage(P2PMessageType MsgType, PeerInfo sender, PeerInfo receiver, List<PeerInfo> peers, String imgName, int imageBlockN, Image img) {
        this.Sender = sender;
        this.Receiver = receiver;
        this.MessageType = MsgType;
        this.ImgName = imgName;
        this.ImageBlockN = imageBlockN;
        if (img != null) {
            this.SerializedImg = new SerializedImage(img);
        } else {
            this.SerializedImg = null;
        }
        this.peers = peers;
    }

    public PeerInfo getSender() {
        return Sender;
    }

    public PeerInfo getReceiver() {
        return Receiver;
    }

    public List<PeerInfo> getPeers() {
        return peers;
    }

    public P2PMessageType getCmd() {
        return MessageType;
    }

    public String getImgName() {
        return ImgName;
    }

    public int getImageBlockN() {
        return ImageBlockN;
    }

    public Image getSerializedImg() {
        return SerializedImg.img;
    }

    /* Request */
    public static P2PMessage reqLogin(PeerInfo sender, PeerInfo receiver) {
        return new P2PMessage(P2PMessageType.requestLogin, sender, receiver, null, null, -1, null);
    }

    public static P2PMessage reqImageUpdate(PeerInfo sender, PeerInfo receiver, List<PeerInfo> peers, String imgName) {
        return new P2PMessage(P2PMessageType.requestImageUpdate, sender, receiver, peers, imgName, -1, null);
    }


    public static P2PMessage reqImageBlock(PeerInfo sender, PeerInfo receiver, List<PeerInfo> peers, String imgName, int imageBlockN) {
        return new P2PMessage(P2PMessageType.requestImgBlk, sender, receiver, peers, imgName, imageBlockN, null);
    }

    public static P2PMessage reqActiveCheck(PeerInfo sender, PeerInfo receiver, List<PeerInfo> peers) {
        return new P2PMessage(P2PMessageType.requestActiveCheck, sender, receiver, peers, null, -1, null);
    }

    /* Response */
    public static P2PMessage resLoginFail(PeerInfo sender, PeerInfo receiver, List<PeerInfo> peers) {
        return new P2PMessage(P2PMessageType.resLoginFail, sender, receiver, peers, null, -1, null);
    }

    public static P2PMessage resLoginOK(PeerInfo sender, PeerInfo receiver, List<PeerInfo> peers, String ImageName) {
        return new P2PMessage(P2PMessageType.resLoginOk, sender, receiver, peers, ImageName, -1, null);
    }

    public static P2PMessage resActiveCheckOK(PeerInfo sender, PeerInfo receiver, List<PeerInfo> peers) {
        return new P2PMessage(P2PMessageType.resActiveOK, sender, receiver, peers, null, -1, null);
    }

    public static P2PMessage resImageUpdateOK(PeerInfo sender, PeerInfo receiver, List<PeerInfo> peers, String imgName) {
        return new P2PMessage(P2PMessageType.resImageUpdateOk, sender, receiver, peers, imgName, -1, null);
    }

    public static P2PMessage resImageBlock(PeerInfo sender, PeerInfo receiver, List<PeerInfo> peers, String imgName, int imageBlockN, Image img) {
        return new P2PMessage(P2PMessageType.resImgBlk, sender, receiver, peers, imgName, imageBlockN, img);
    }

    public static P2PMessage resImageBlockNotAvailable(PeerInfo sender, PeerInfo receiver, List<PeerInfo> peers, String imgName) {
        return new P2PMessage(P2PMessageType.resImgBlkNotAvailable, sender, receiver, peers, imgName, -1, null);
    }

    public static P2PMessage resImageNameMismatch(PeerInfo sender, PeerInfo receiver, List<PeerInfo> peers, String imgName, int imageBlockN, Image img) {
        return new P2PMessage(P2PMessageType.resImgNameMismatch, sender, receiver, peers, imgName, imageBlockN, img);
    }
}

class SerializedImage implements Serializable {
    //    transient List<BufferedImage> images;
    transient Image img;

    public SerializedImage(Image img) {
        this.img = img;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ImageIO.write((BufferedImage) img, "png", out);
//        out.writeInt(images.size()); // how many images are serialized?
//        for (BufferedImage eachImage : images) {
//            ImageIO.write(eachImage, "png", out); // png is lossless
//        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        img = ImageIO.read(in);
    }


}
