package share;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PeerInfoTest {
    Set<PeerInfo> t = new HashSet<>();

    @Test
    void name() {
        PeerInfo p1 = new PeerInfo("localhost", "STUDENT", "aa", 8001);
        PeerInfo p2 = new PeerInfo("localhost", "STUDENT", "aa", 8001);


        System.out.println(p1.hashCode());
        System.out.println(p2.hashCode());


        System.out.println(p1.equals(p2));

        t.add(p1);
        t.add(p2);

        System.out.println(t);
    }
}