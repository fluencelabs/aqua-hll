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

// Functions
export const streamFold_script = `
                    (xor
                     (seq
                      (seq
                       (seq
                        (call %init_peer_id% ("getDataSrv" "-relay-") [] -relay-)
                        (call %init_peer_id% ("getDataSrv" "arr") [] arr)
                       )
                       (new $res
                        (seq
                         (seq
                          (fold arr n-0
                           (seq
                            (ap n-0 $res)
                            (next n-0)
                           )
                          )
                          (canon %init_peer_id% $res  #-res-fix-0)
                         )
                         (ap #-res-fix-0 -res-flat-0)
                        )
                       )
                      )
                      (xor
                       (call %init_peer_id% ("callbackSrv" "response") [-res-flat-0])
                       (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 1])
                      )
                     )
                     (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 2])
                    )
    `
 

export function streamFold(
    arr: string[],
    config?: {ttl?: number}
): Promise<string[]>;

export function streamFold(
    peer: IFluenceClient$$,
    arr: string[],
    config?: {ttl?: number}
): Promise<string[]>;

export function streamFold(...args: any) {


    return callFunction$$(
        args,
        {
    "functionName" : "streamFold",
    "arrow" : {
        "tag" : "arrow",
        "domain" : {
            "tag" : "labeledProduct",
            "fields" : {
                "arr" : {
                    "tag" : "array",
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
                    "tag" : "array",
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
        streamFold_script
    )
}

export const streamRes_script = `
                    (xor
                     (seq
                      (seq
                       (seq
                        (call %init_peer_id% ("getDataSrv" "-relay-") [] -relay-)
                        (call %init_peer_id% ("getDataSrv" "arr") [] arr)
                       )
                       (new $res
                        (seq
                         (seq
                          (new $res-0
                           (seq
                            (seq
                             (fold arr n-0
                              (seq
                               (ap n-0 $res-0)
                               (next n-0)
                              )
                             )
                             (canon %init_peer_id% $res-0  #-res-fix-0-0)
                            )
                            (ap #-res-fix-0-0 -res-flat-0-0)
                           )
                          )
                          (canon %init_peer_id% $res  #-res-fix-0)
                         )
                         (ap #-res-fix-0 -res-flat-0)
                        )
                       )
                      )
                      (xor
                       (call %init_peer_id% ("callbackSrv" "response") [-res-flat-0 -res-flat-0-0])
                       (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 1])
                      )
                     )
                     (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 2])
                    )
    `
 
export type StreamResResult = [string[], string[]]
export function streamRes(
    arr: string[],
    config?: {ttl?: number}
): Promise<StreamResResult>;

export function streamRes(
    peer: IFluenceClient$$,
    arr: string[],
    config?: {ttl?: number}
): Promise<StreamResResult>;

export function streamRes(...args: any) {


    return callFunction$$(
        args,
        {
    "functionName" : "streamRes",
    "arrow" : {
        "tag" : "arrow",
        "domain" : {
            "tag" : "labeledProduct",
            "fields" : {
                "arr" : {
                    "tag" : "array",
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
                    "tag" : "array",
                    "type" : {
                        "tag" : "scalar",
                        "name" : "string"
                    }
                },
                {
                    "tag" : "array",
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
        streamRes_script
    )
}

/* eslint-enable */