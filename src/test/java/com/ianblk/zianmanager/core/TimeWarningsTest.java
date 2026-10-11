package com.ianblk.zianmanager.core;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class TimeWarningsTest {
 @TempDir Path dir;
 private final UUID player=UUID.randomUUID();private final LocalDate day=LocalDate.of(2026,10,9);
 @Test void eachThresholdWarnsOnceAndSurvivesRestart()throws Exception{
  var file=dir.resolve("warnings.json");var w=new TimeWarnings(file);
  assertTrue(w.poll(player,day,301).isEmpty());assertEquals(300,w.poll(player,day,300).orElseThrow());
  w=new TimeWarnings(file);assertTrue(w.poll(player,day,299).isEmpty());assertEquals(60,w.poll(player,day,60).orElseThrow());
  assertTrue(w.poll(player,day,59).isEmpty());assertEquals(30,w.poll(player,day,30).orElseThrow());assertTrue(w.poll(player,day,29).isEmpty());assertTrue(w.poll(player,day,0).isEmpty());
 }
 @Test void lagSkippingSeveralThresholdsSendsOnlyCurrentBalance()throws Exception{
  var w=new TimeWarnings(dir.resolve("warnings.json"));w.poll(player,day,400);assertEquals(20,w.poll(player,day,20).orElseThrow());assertTrue(w.poll(player,day,19).isEmpty());
 }
 @Test void shortQuotaDoesNotSendHigherWarningsLater()throws Exception{
  var w=new TimeWarnings(dir.resolve("warnings.json"));assertEquals(59,w.poll(player,day,59).orElseThrow());assertTrue(w.poll(player,day,58).isEmpty());assertEquals(30,w.poll(player,day,30).orElseThrow());
 }
 @Test void bonusRearmsOnlyThresholdsAboveThePreviousBalance()throws Exception{
  var w=new TimeWarnings(dir.resolve("warnings.json"));w.poll(player,day,20);assertTrue(w.poll(player,day,120).isEmpty());
  assertEquals(60,w.poll(player,day,60).orElseThrow());assertTrue(w.poll(player,day,59).isEmpty());assertEquals(30,w.poll(player,day,30).orElseThrow());
  assertTrue(w.poll(player,day,1800).isEmpty());assertEquals(300,w.poll(player,day,300).orElseThrow());
 }
 @Test void newDayAndOtherPlayersHaveIndependentWarnings()throws Exception{
  var w=new TimeWarnings(dir.resolve("warnings.json"));w.poll(player,day,30);assertEquals(30,w.poll(player,day.plusDays(1),30).orElseThrow());assertEquals(30,w.poll(UUID.randomUUID(),day,30).orElseThrow());
 }
}
