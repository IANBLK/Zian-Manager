package com.ianblk.zianmanager.core;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ChestDeliveryDelayTest {
 @Test void repeatedClicksCannotDeliverEarlyExtendTheDelayOrCreateAnotherDelivery(){var gate=new ChestDeliveryDelay();var owner=UUID.randomUUID();var id=UUID.randomUUID();assertTrue(gate.schedule(owner,id,100));for(int i=0;i<25;i++){assertFalse(gate.schedule(owner,id,100+i));assertTrue(gate.blocked(owner,id,100+i));}assertTrue(gate.ready(129).isEmpty());assertEquals(1,gate.ready(130).size());assertTrue(gate.ready(130).isEmpty());}
 @Test void onePlayersClaimsCannotBeScheduledByAnother(){var gate=new ChestDeliveryDelay();var id=UUID.randomUUID();gate.schedule(UUID.randomUUID(),id,0);assertThrows(IllegalArgumentException.class,()->gate.schedule(UUID.randomUUID(),id,0));}
}
