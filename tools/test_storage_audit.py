import importlib.util
import json
from pathlib import Path
import tempfile
import unittest

spec = importlib.util.spec_from_file_location("storage_audit", Path(__file__).with_name("storage-audit.py"))
audit = importlib.util.module_from_spec(spec)
spec.loader.exec_module(audit)


class StorageAuditTest(unittest.TestCase):
    def test_pending_receipts_are_counted_without_printing_players_or_changing_files(self):
        with tempfile.TemporaryDirectory() as folder:
            world = Path(folder)
            path = world / "data/zianmanager/claims.json"
            path.parent.mkdir(parents=True)
            path.write_text(json.dumps({"claims": [
                {"player": "PRIVATE_PLAYER", "parts": [{"phase": "DELIVERED"}]},
                {"player": "PRIVATE_PLAYER", "parts": [{"phase": "APPLYING"}]}]}))
            original = path.read_bytes()
            result = audit.inventory(world)
            self.assertEqual(1, result["sharedLedgers"]["manager_rewards"]["pendingOrReview"])
            self.assertNotIn("PRIVATE_PLAYER", json.dumps(result))
            self.assertEqual(original, path.read_bytes())

    def test_invalid_data_is_reported_without_repair(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / "data/zianmanager/claims.json"
            path.parent.mkdir(parents=True)
            path.write_bytes(b"not-json")
            self.assertIn("warning", audit.inventory(folder)["sharedLedgers"]["manager_rewards"])
            self.assertEqual(b"not-json", path.read_bytes())


if __name__ == "__main__":
    unittest.main()
