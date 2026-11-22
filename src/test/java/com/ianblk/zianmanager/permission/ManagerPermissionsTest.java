package com.ianblk.zianmanager.permission;

import com.ianblk.zianmanager.permission.LuckPermsLookup.Decision;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManagerPermissionsTest {
    @Test void explicitTrueWinsOverFallback() { assertTrue(ManagerPermissions.evaluate(false, () -> Decision.TRUE)); }
    @Test void explicitFalseWinsOverFallback() { assertFalse(ManagerPermissions.evaluate(true, () -> Decision.FALSE)); }
    @Test void absentAndUndefinedUseFallback() {
        for (Decision d : new Decision[] {Decision.ABSENT, Decision.UNDEFINED}) {
            assertTrue(ManagerPermissions.evaluate(true, () -> d));
            assertFalse(ManagerPermissions.evaluate(false, () -> d));
        }
    }
    @Test void runtimeFailureDenies() {
        assertFalse(ManagerPermissions.evaluate(true, () -> { throw new IllegalStateException("x"); }));
    }
    @Test void linkageErrorDenies() {
        assertFalse(ManagerPermissions.evaluate(true, () -> { throw new NoClassDefFoundError("x"); }));
    }
}
