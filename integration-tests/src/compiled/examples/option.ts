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

export interface SomeSDef {
    checkU32: (u: number | null, callParams: CallParams$$<'u'>) => void | Promise<void>;
    getStr: (arg0: string | null, callParams: CallParams$$<'arg0'>) => string | null | Promise<string | null>;
    getStr1: (callParams: CallParams$$<null>) => string | null | Promise<string | null>;
    getStr2: (arg0: string, callParams: CallParams$$<'arg0'>) => string | Promise<string>;
}
export function registerSomeS(service: SomeSDef): void;
export function registerSomeS(serviceId: string, service: SomeSDef): void;
export function registerSomeS(peer: IFluenceClient$$, service: SomeSDef): void;
export function registerSomeS(peer: IFluenceClient$$, serviceId: string, service: SomeSDef): void;
       

export function registerSomeS(...args: any) {
    registerService$$(
        args,
        {
    "defaultServiceId" : "test2",
    "functions" : {
        "tag" : "labeledProduct",
        "fields" : {
            "checkU32" : {
                "tag" : "arrow",
                "domain" : {
                    "tag" : "labeledProduct",
                    "fields" : {
                        "u" : {
                            "tag" : "option",
                            "type" : {
                                "tag" : "scalar",
                                "name" : "u32"
                            }
                        }
                    }
                },
                "codomain" : {
                    "tag" : "nil"
                }
            },
            "getStr" : {
                "tag" : "arrow",
                "domain" : {
                    "tag" : "unlabeledProduct",
                    "items" : [
                        {
                            "tag" : "option",
                            "type" : {
                                "tag" : "scalar",
                                "name" : "string"
                            }
                        }
                    ]
                },
                "codomain" : {
                    "tag" : "unlabeledProduct",
                    "items" : [
                        {
                            "tag" : "option",
                            "type" : {
                                "tag" : "scalar",
                                "name" : "string"
                            }
                        }
                    ]
                }
            },
            "getStr1" : {
                "tag" : "arrow",
                "domain" : {
                    "tag" : "nil"
                },
                "codomain" : {
                    "tag" : "unlabeledProduct",
                    "items" : [
                        {
                            "tag" : "option",
                            "type" : {
                                "tag" : "scalar",
                                "name" : "string"
                            }
                        }
                    ]
                }
            },
            "getStr2" : {
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
                            "name" : "string"
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
export const returnNone_script = `
                    (xor
                     (seq
                      (seq
                       (call %init_peer_id% ("getDataSrv" "-relay-") [] -relay-)
                       (new $result
                        (seq
                         (seq
                          (call %init_peer_id% ("op" "noop") [])
                          (canon %init_peer_id% $result  #-result-fix-0)
                         )
                         (ap #-result-fix-0 -result-flat-0)
                        )
                       )
                      )
                      (xor
                       (call %init_peer_id% ("callbackSrv" "response") [-result-flat-0])
                       (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 1])
                      )
                     )
                     (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 2])
                    )
    `
 

export function returnNone(
    config?: {ttl?: number}
): Promise<string | null>;

export function returnNone(
    peer: IFluenceClient$$,
    config?: {ttl?: number}
): Promise<string | null>;

export function returnNone(...args: any) {


    return callFunction$$(
        args,
        {
    "functionName" : "returnNone",
    "arrow" : {
        "tag" : "arrow",
        "domain" : {
            "tag" : "labeledProduct",
            "fields" : {
                
            }
        },
        "codomain" : {
            "tag" : "unlabeledProduct",
            "items" : [
                {
                    "tag" : "option",
                    "type" : {
                        "tag" : "scalar",
                        "name" : "string"
                    }
                }
            ]
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
        returnNone_script
    )
}

export const checkU32AndU8_script = `
                    (xor
                     (seq
                      (seq
                       (call %init_peer_id% ("getDataSrv" "-relay-") [] -relay-)
                       (call %init_peer_id% ("getDataSrv" "a") [] a)
                      )
                      (call %init_peer_id% ("test2" "checkU32") [a])
                     )
                     (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 1])
                    )
    `
 

export function checkU32AndU8(
    a: number | null,
    config?: {ttl?: number}
): Promise<void>;

export function checkU32AndU8(
    peer: IFluenceClient$$,
    a: number | null,
    config?: {ttl?: number}
): Promise<void>;

export function checkU32AndU8(...args: any) {


    return callFunction$$(
        args,
        {
    "functionName" : "checkU32AndU8",
    "arrow" : {
        "tag" : "arrow",
        "domain" : {
            "tag" : "labeledProduct",
            "fields" : {
                "a" : {
                    "tag" : "option",
                    "type" : {
                        "tag" : "scalar",
                        "name" : "u8"
                    }
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
        checkU32AndU8_script
    )
}

export const useOptional_script = `
                    (xor
                     (seq
                      (seq
                       (seq
                        (seq
                         (call %init_peer_id% ("getDataSrv" "-relay-") [] -relay-)
                         (call %init_peer_id% ("getDataSrv" "opt") [] opt)
                        )
                        (call %init_peer_id% ("test2" "getStr") [opt] res)
                       )
                       (fold opt i-0
                        (seq
                         (call %init_peer_id% ("test2" "getStr2") [i-0])
                         (next i-0)
                        )
                       )
                      )
                      (xor
                       (call %init_peer_id% ("callbackSrv" "response") [res.$.[0]])
                       (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 1])
                      )
                     )
                     (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 2])
                    )
    `
 

export function useOptional(
    opt: string | null,
    config?: {ttl?: number}
): Promise<string>;

export function useOptional(
    peer: IFluenceClient$$,
    opt: string | null,
    config?: {ttl?: number}
): Promise<string>;

export function useOptional(...args: any) {


    return callFunction$$(
        args,
        {
    "functionName" : "useOptional",
    "arrow" : {
        "tag" : "arrow",
        "domain" : {
            "tag" : "labeledProduct",
            "fields" : {
                "opt" : {
                    "tag" : "option",
                    "type" : {
                        "tag" : "scalar",
                        "name" : "string"
                    }
                }
            }
        },
        "codomain" : {
            "tag" : "unlabeledProduct",
            "items" : [
                {
                    "tag" : "scalar",
                    "name" : "string"
                }
            ]
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
        useOptional_script
    )
}

export const returnOptional_script = `
                    (xor
                     (seq
                      (seq
                       (call %init_peer_id% ("getDataSrv" "-relay-") [] -relay-)
                       (call %init_peer_id% ("test2" "getStr1") [] res)
                      )
                      (xor
                       (call %init_peer_id% ("callbackSrv" "response") [res])
                       (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 1])
                      )
                     )
                     (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 2])
                    )
    `
 

export function returnOptional(
    config?: {ttl?: number}
): Promise<string | null>;

export function returnOptional(
    peer: IFluenceClient$$,
    config?: {ttl?: number}
): Promise<string | null>;

export function returnOptional(...args: any) {


    return callFunction$$(
        args,
        {
    "functionName" : "returnOptional",
    "arrow" : {
        "tag" : "arrow",
        "domain" : {
            "tag" : "labeledProduct",
            "fields" : {
                
            }
        },
        "codomain" : {
            "tag" : "unlabeledProduct",
            "items" : [
                {
                    "tag" : "option",
                    "type" : {
                        "tag" : "scalar",
                        "name" : "string"
                    }
                }
            ]
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
        returnOptional_script
    )
}

/* eslint-enable */