package com.ianblk.zianmanager.core;
import com.ianblk.zianmanager.core.TooltipLayout.Kind;
import com.ianblk.zianmanager.core.TooltipLayout.Spec;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class TooltipLayoutTest {
 private static List<Kind> kinds(Spec s,boolean x){return TooltipLayout.lines(s,x).stream().map(TooltipLayout.Line::kind).toList();}
 @Test void collapsedWithoutAbilityShowsKindLoreAndHint(){assertEquals(List.of(Kind.KIND,Kind.LORE,Kind.HINT),kinds(new Spec("hammer",0,0),false));}
 @Test void collapsedWithAbilityAddsCooldownBeforeHint(){var l=TooltipLayout.lines(new Spec("ninja_sword",30,90),false);assertEquals(List.of(Kind.KIND,Kind.LORE,Kind.COOLDOWN,Kind.HINT),l.stream().map(TooltipLayout.Line::kind).toList());assertEquals(90,l.get(2).arg());}
 @Test void expandedIsExactlyTheDocumentedOrder(){var l=TooltipLayout.lines(new Spec("epic_sword",30,90),true);assertEquals(List.of(Kind.KIND,Kind.LORE,Kind.LORE,Kind.BLANK,Kind.LEGEND,Kind.DETAIL,Kind.ABILITY,Kind.DURATION,Kind.COOLDOWN,Kind.RARITY),l.stream().map(TooltipLayout.Line::kind).toList());
  assertEquals("flavor.zianmanager.epic_sword.line2",l.get(2).key());assertEquals("flavor.zianmanager.epic_sword.legend",l.get(4).key());assertEquals(30,l.get(7).arg());assertEquals(90,l.get(8).arg());assertEquals("tooltip.zianmanager.rarity.epic",l.get(9).key());}
 @Test void zeroDurationIsOmitted(){assertFalse(kinds(new Spec("flame_sword",0,1),true).contains(Kind.DURATION));assertTrue(kinds(new Spec("flame_sword",0,1),true).contains(Kind.ABILITY));}
 @Test void expandedWithoutAbilityOrRarityHasNoAbilityBlock(){assertEquals(List.of(Kind.KIND,Kind.LORE,Kind.LORE,Kind.BLANK,Kind.LEGEND,Kind.DETAIL),kinds(new Spec("hammer",0,0),true));}
 @Test void rarityById(){assertEquals("common",TooltipLayout.rarity("common_crate"));assertEquals("rare",TooltipLayout.rarity("rare_key"));assertEquals("epic",TooltipLayout.rarity("epic_key"));assertEquals("cosmetic",TooltipLayout.rarity("cosmetic_key"));assertNull(TooltipLayout.rarity("reimagined_rare_sword"));assertNull(TooltipLayout.rarity("ninja_sword"));}
}
