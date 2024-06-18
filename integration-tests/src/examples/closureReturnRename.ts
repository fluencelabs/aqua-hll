import { lng193Bug } from "../compiled/examples/closureReturnRename.js";
import { config } from "../config.js";

const relays = config.relays;

export async function lng193BugCall(): Promise<number> {
  return lng193Bug(relays[4].peerId, relays[5].peerId);
}

export async function lng365BugCall(): Promise<number> {
  return lng365Bug();
}
