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

export interface NodeIdGetterDef {
    get: (callParams: CallParams$$<null>) => { name: string; peerId: string; } | Promise<{ name: string; peerId: string; }>;
}
export function registerNodeIdGetter(service: NodeIdGetterDef): void;
export function registerNodeIdGetter(serviceId: string, service: NodeIdGetterDef): void;
export function registerNodeIdGetter(peer: IFluenceClient$$, service: NodeIdGetterDef): void;
export function registerNodeIdGetter(peer: IFluenceClient$$, serviceId: string, service: NodeIdGetterDef): void;
       

export function registerNodeIdGetter(...args: any) {
    registerService$$(
        args,
        {
    "defaultServiceId" : "somesrv",
    "functions" : {
        "tag" : "labeledProduct",
        "fields" : {
            "get" : {
                "tag" : "arrow",
                "domain" : {
                    "tag" : "nil"
                },
                "codomain" : {
                    "tag" : "unlabeledProduct",
                    "items" : [
                        {
                            "tag" : "struct",
                            "name" : "NodeId",
                            "fields" : {
                                "name" : {
                                    "tag" : "scalar",
                                    "name" : "string"
                                },
                                "peerId" : {
                                    "tag" : "scalar",
                                    "name" : "string"
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
      
// Functions
export const getAliasedData_script = `
                    (xor
                     (seq
                      (seq
                       (seq
                        (call %init_peer_id% ("getDataSrv" "-relay-") [] -relay-)
                        (call %init_peer_id% ("somesrv" "get") [] res)
                       )
                       (ap res.$.peerId res_flat)
                      )
                      (xor
                       (call %init_peer_id% ("callbackSrv" "response") [res_flat])
                       (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 1])
                      )
                     )
                     (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 2])
                    )
    `
 

export function getAliasedData(
    config?: {ttl?: number}
): Promise<string>;

export function getAliasedData(
    peer: IFluenceClient$$,
    config?: {ttl?: number}
): Promise<string>;

export function getAliasedData(...args: any) {


    return callFunction$$(
        args,
        {
    "functionName" : "getAliasedData",
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
        getAliasedData_script
    )
}

/* eslint-enable */