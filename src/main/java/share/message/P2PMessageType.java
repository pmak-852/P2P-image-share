package share.message;

public enum P2PMessageType {
    requestLogin, resLoginFail, resLoginOk,
    requestImgBlk, resImgBlk, resImgBlkNotAvailable, resImgNameMismatch,
    requestImageUpdate, resImageUpdateOk,
    requestActiveCheck, resActiveOK
}
