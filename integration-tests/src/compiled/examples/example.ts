/* eslint-disable */
// @ts-nocheck
/**
 *
 * This file is auto-generated. Do not edit manually: changes may be erased.
 * Generated by Aqua compiler: https://github.com/fluencelabs/aqua/.
 * If you find any bugs, please write an issue on GitHub: https://github.com/fluencelabs/aqua/issues
 * Aqua version: 0.11.5
 *
 */
import type { IFluenceClient as IFluenceClient$$, CallParams as CallParams$$ } from '@fluencelabs/js-client.api';
import {
    v5_callFunction as callFunction$$,
    v5_registerService as registerService$$,
} from '@fluencelabs/js-client.api';
    


// Services

export interface OpDef {
    identity: (callParams: CallParams$$<null>) => void | Promise<void>;
}
export function registerOp(service: OpDef): void;
export function registerOp(serviceId: string, service: OpDef): void;
export function registerOp(peer: IFluenceClient$$, service: OpDef): void;
export function registerOp(peer: IFluenceClient$$, serviceId: string, service: OpDef): void;
       

export function registerOp(...args: any) {
    registerService$$(
        args,
        {
    "defaultServiceId" : "op",
    "functions" : {
        "tag" : "labeledProduct",
        "fields" : {
            "identity" : {
                "tag" : "arrow",
                "domain" : {
                    "tag" : "nil"
                },
                "codomain" : {
                    "tag" : "nil"
                }
            }
        }
    }
}
    );
}
      


export interface TestDef {
    doSomething: (callParams: CallParams$$<null>) => boolean | Promise<boolean>;
    getUserList: (callParams: CallParams$$<null>) => { name: string; peer_id: string; relay_id: string; }[] | Promise<{ name: string; peer_id: string; relay_id: string; }[]>;
}
export function registerTest(service: TestDef): void;
export function registerTest(serviceId: string, service: TestDef): void;
export function registerTest(peer: IFluenceClient$$, service: TestDef): void;
export function registerTest(peer: IFluenceClient$$, serviceId: string, service: TestDef): void;
       

export function registerTest(...args: any) {
    registerService$$(
        args,
        {
    "defaultServiceId" : "test",
    "functions" : {
        "tag" : "labeledProduct",
        "fields" : {
            "doSomething" : {
                "tag" : "arrow",
                "domain" : {
                    "tag" : "nil"
                },
                "codomain" : {
                    "tag" : "unlabeledProduct",
                    "items" : [
                        {
                            "tag" : "scalar",
                            "name" : "bool"
                        }
                    ]
                }
            },
            "getUserList" : {
                "tag" : "arrow",
                "domain" : {
                    "tag" : "nil"
                },
                "codomain" : {
                    "tag" : "unlabeledProduct",
                    "items" : [
                        {
                            "tag" : "array",
                            "type" : {
                                "tag" : "struct",
                                "name" : "User",
                                "fields" : {
                                    "name" : {
                                        "tag" : "scalar",
                                        "name" : "string"
                                    },
                                    "peer_id" : {
                                        "tag" : "scalar",
                                        "name" : "string"
                                    },
                                    "relay_id" : {
                                        "tag" : "scalar",
                                        "name" : "string"
                                    }
                                }
                            }
                        }
                    ]
                }
            }
        }
    }
}
    );
}
      


export interface PeerDef {
    is_connected: (arg0: string, callParams: CallParams$$<'arg0'>) => boolean | Promise<boolean>;
}
export function registerPeer(service: PeerDef): void;
export function registerPeer(serviceId: string, service: PeerDef): void;
export function registerPeer(peer: IFluenceClient$$, service: PeerDef): void;
export function registerPeer(peer: IFluenceClient$$, serviceId: string, service: PeerDef): void;
       

export function registerPeer(...args: any) {
    registerService$$(
        args,
        {
    "defaultServiceId" : "peer",
    "functions" : {
        "tag" : "labeledProduct",
        "fields" : {
            "is_connected" : {
                "tag" : "arrow",
                "domain" : {
                    "tag" : "unlabeledProduct",
                    "items" : [
                        {
                            "tag" : "scalar",
                            "name" : "string"
                        }
                    ]
                },
                "codomain" : {
                    "tag" : "unlabeledProduct",
                    "items" : [
                        {
                            "tag" : "scalar",
                            "name" : "bool"
                        }
                    ]
                }
            }
        }
    }
}
    );
}
      
// Functions
export const betterMessage_script = `
                    (xor
                     (seq
                      (seq
                       (seq
                        (seq
                         (call %init_peer_id% ("getDataSrv" "-relay-") [] -relay-)
                         (call %init_peer_id% ("getDataSrv" "relay") [] relay)
                        )
                        (call -relay- ("op" "noop") [])
                       )
                       (xor
                        (seq
                         (call relay ("peer" "is_connected") [relay] isOnline)
                         (call -relay- ("op" "noop") [])
                        )
                        (seq
                         (call -relay- ("op" "noop") [])
                         (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 1])
                        )
                       )
                      )
                      (xor
                       (match isOnline true
                        (xor
                         (call %init_peer_id% ("test" "doSomething") [])
                         (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 2])
                        )
                       )
                       (call %init_peer_id% ("op" "noop") [])
                      )
                     )
                     (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 3])
                    )
    `
 

export function betterMessage(
    relay: string,
    config?: {ttl?: number}
): Promise<void>;

export function betterMessage(
    peer: IFluenceClient$$,
    relay: string,
    config?: {ttl?: number}
): Promise<void>;

export function betterMessage(...args: any) {


    return callFunction$$(
        args,
        {
    "functionName" : "betterMessage",
    "arrow" : {
        "tag" : "arrow",
        "domain" : {
            "tag" : "labeledProduct",
            "fields" : {
                "relay" : {
                    "tag" : "scalar",
                    "name" : "string"
                }
            }
        },
        "codomain" : {
            "tag" : "nil"
        }
    },
    "names" : {
        "relay" : "-relay-",
        "getDataSrv" : "getDataSrv",
        "callbackSrv" : "callbackSrv",
        "responseSrv" : "callbackSrv",
        "responseFnName" : "response",
        "errorHandlingSrv" : "errorHandlingSrv",
        "errorFnName" : "error"
    }
},
        betterMessage_script
    )
}

/* eslint-enable */