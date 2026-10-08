package com.ianblk.zianmanager.core;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class KeyUseGateTest {
 @Test void rapidClicksDoNotExtendWaitAndOtherPlayersAreIndependent(){var gate=new KeyUseGate();var p=UUID.randomUUID();assertEquals(0,gate.enter(p,100));for(long tick=100;tick<140;tick++)assertEquals(140-tick,gate.enter(p,tick));assertEquals(0,gate.enter(UUID.randomUUID(),110));assertEquals(0,gate.enter(p,140));assertEquals(40,gate.enter(p,140));gate.clear();assertEquals(0,gate.enter(p,140));}
}
