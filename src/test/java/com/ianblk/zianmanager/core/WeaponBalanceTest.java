package com.ianblk.zianmanager.core;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class WeaponBalanceTest {
    @Test void decisionD8ValuesAreFixed(){assertEquals(10,WeaponBalance.FLAME_COOLDOWN_SECONDS);assertEquals(7,WeaponBalance.FLAME_DAMAGE);assertEquals(6,WeaponBalance.FLAME_FIRE_SECONDS);assertEquals(20,WeaponBalance.EFFECT_SECONDS);assertEquals(120,WeaponBalance.EFFECT_COOLDOWN_SECONDS);assertEquals(10,WeaponBalance.SONIC_COOLDOWN_SECONDS);}
}
