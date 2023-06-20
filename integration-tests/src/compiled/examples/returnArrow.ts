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
export const returnCall_script = `
                    (xor
                     (seq
                      (seq
                       (seq
                        (call %init_peer_id% ("getDataSrv" "-relay-") [] -relay-)
                        (call %init_peer_id% ("getDataSrv" "arg") [] arg)
                       )
                       (call %init_peer_id% ("op" "concat_strings") [arg " literal"] str)
                      )
                      (xor
                       (call %init_peer_id% ("callbackSrv" "response") [closure])
                       (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 1])
                      )
                     )
                     (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 2])
                    )
    `
 

export function returnCall(
    arg: string,
    config?: {ttl?: number}
): Promise<(arg0: string, callParams: CallParams$$<'arg0'>) => [string, string] | Promise<[string, string]>>;

export function returnCall(
    peer: IFluenceClient$$,
    arg: string,
    config?: {ttl?: number}
): Promise<(arg0: string, callParams: CallParams$$<'arg0'>) => [string, string] | Promise<[string, string]>>;

export function returnCall(...args: any) {


    return callFunction$$(
        args,
        {
    "functionName" : "returnCall",
    "arrow" : {
        "tag" : "arrow",
        "domain" : {
            "tag" : "labeledProduct",
            "fields" : {
                "arg" : {
                    "tag" : "scalar",
                    "name" : "string"
                }
            }
        },
        "codomain" : {
            "tag" : "unlabeledProduct",
            "items" : [
                {
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
                            },
                            {
                                "tag" : "scalar",
                                "name" : "string"
                            }
                        ]
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
        returnCall_script
    )
}

export const callReturnedArrow_script = `
                    (xor
                     (seq
                      (seq
                       (seq
                        (seq
                         (seq
                          (call %init_peer_id% ("getDataSrv" "-relay-") [] -relay-)
                          (call %init_peer_id% ("getDataSrv" "argForFunc") [] argForFunc)
                         )
                         (call %init_peer_id% ("getDataSrv" "argForClosure") [] argForClosure)
                        )
                        (call %init_peer_id% ("op" "concat_strings") [argForFunc " literal"] str)
                       )
                       (call %init_peer_id% ("op" "concat_strings") [argForClosure str] concat_strings)
                      )
                      (xor
                       (call %init_peer_id% ("callbackSrv" "response") [argForClosure concat_strings])
                       (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 1])
                      )
                     )
                     (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 2])
                    )
    `
 
export type CallReturnedArrowResult = [string, string]
export function callReturnedArrow(
    argForFunc: string,
    argForClosure: string,
    config?: {ttl?: number}
): Promise<CallReturnedArrowResult>;

export function callReturnedArrow(
    peer: IFluenceClient$$,
    argForFunc: string,
    argForClosure: string,
    config?: {ttl?: number}
): Promise<CallReturnedArrowResult>;

export function callReturnedArrow(...args: any) {


    return callFunction$$(
        args,
        {
    "functionName" : "callReturnedArrow",
    "arrow" : {
        "tag" : "arrow",
        "domain" : {
            "tag" : "labeledProduct",
            "fields" : {
                "argForFunc" : {
                    "tag" : "scalar",
                    "name" : "string"
                },
                "argForClosure" : {
                    "tag" : "scalar",
                    "name" : "string"
                }
            }
        },
        "codomain" : {
            "tag" : "unlabeledProduct",
            "items" : [
                {
                    "tag" : "scalar",
                    "name" : "string"
                },
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
        callReturnedArrow_script
    )
}

export const secondReturnCall_script = `
                    (xor
                     (seq
                      (seq
                       (seq
                        (seq
                         (call %init_peer_id% ("getDataSrv" "-relay-") [] -relay-)
                         (call %init_peer_id% ("getDataSrv" "arg") [] arg)
                        )
                        (call %init_peer_id% ("op" "concat_strings") [arg " second literal"] str)
                       )
                       (call %init_peer_id% ("op" "concat_strings") [" from second" " literal"] str-0)
                      )
                      (xor
                       (call %init_peer_id% ("callbackSrv" "response") [closure closure closure-0])
                       (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 1])
                      )
                     )
                     (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 2])
                    )
    `
 
export type SecondReturnCallResult = [(arg0: string, callParams: CallParams$$<'arg0'>) => [string, string] | Promise<[string, string]>, (arg0: string, callParams: CallParams$$<'arg0'>) => [string, string] | Promise<[string, string]>, (arg0: string, callParams: CallParams$$<'arg0'>) => [string, string] | Promise<[string, string]>]
export function secondReturnCall(
    arg: string,
    config?: {ttl?: number}
): Promise<SecondReturnCallResult>;

export function secondReturnCall(
    peer: IFluenceClient$$,
    arg: string,
    config?: {ttl?: number}
): Promise<SecondReturnCallResult>;

export function secondReturnCall(...args: any) {


    return callFunction$$(
        args,
        {
    "functionName" : "secondReturnCall",
    "arrow" : {
        "tag" : "arrow",
        "domain" : {
            "tag" : "labeledProduct",
            "fields" : {
                "arg" : {
                    "tag" : "scalar",
                    "name" : "string"
                }
            }
        },
        "codomain" : {
            "tag" : "unlabeledProduct",
            "items" : [
                {
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
                            },
                            {
                                "tag" : "scalar",
                                "name" : "string"
                            }
                        ]
                    }
                },
                {
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
                            },
                            {
                                "tag" : "scalar",
                                "name" : "string"
                            }
                        ]
                    }
                },
                {
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
                            },
                            {
                                "tag" : "scalar",
                                "name" : "string"
                            }
                        ]
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
        secondReturnCall_script
    )
}

export const callReturnedChainArrow_script = `
                    (xor
                     (seq
                      (seq
                       (seq
                        (seq
                         (seq
                          (seq
                           (seq
                            (seq
                             (seq
                              (seq
                               (call %init_peer_id% ("getDataSrv" "-relay-") [] -relay-)
                               (call %init_peer_id% ("getDataSrv" "argForFirst") [] argForFirst)
                              )
                              (call %init_peer_id% ("getDataSrv" "argForSecond") [] argForSecond)
                             )
                             (call %init_peer_id% ("op" "concat_strings") [argForFirst " literal"] str)
                            )
                            (call %init_peer_id% ("op" "concat_strings") [argForSecond " second literal"] str-0)
                           )
                           (call %init_peer_id% ("op" "concat_strings") [" from second" " literal"] str-1)
                          )
                          (call %init_peer_id% ("op" "concat_strings") ["first" str] concat_strings)
                         )
                         (call %init_peer_id% ("op" "concat_strings") ["second" str-0] concat_strings-0)
                        )
                        (call %init_peer_id% ("op" "concat_strings") ["third" str-0] concat_strings-1)
                       )
                       (call %init_peer_id% ("op" "concat_strings") ["fourth" str-1] concat_strings-2)
                      )
                      (xor
                       (call %init_peer_id% ("callbackSrv" "response") ["first" concat_strings "second" concat_strings-0 "third" concat_strings-1 "fourth" concat_strings-2])
                       (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 1])
                      )
                     )
                     (call %init_peer_id% ("errorHandlingSrv" "error") [%last_error% 2])
                    )
    `
 
export type CallReturnedChainArrowResult = [string, string, string, string, string, string, string, string]
export function callReturnedChainArrow(
    argForFirst: string,
    argForSecond: string,
    config?: {ttl?: number}
): Promise<CallReturnedChainArrowResult>;

export function callReturnedChainArrow(
    peer: IFluenceClient$$,
    argForFirst: string,
    argForSecond: string,
    config?: {ttl?: number}
): Promise<CallReturnedChainArrowResult>;

export function callReturnedChainArrow(...args: any) {


    return callFunction$$(
        args,
        {
    "functionName" : "callReturnedChainArrow",
    "arrow" : {
        "tag" : "arrow",
        "domain" : {
            "tag" : "labeledProduct",
            "fields" : {
                "argForFirst" : {
                    "tag" : "scalar",
                    "name" : "string"
                },
                "argForSecond" : {
                    "tag" : "scalar",
                    "name" : "string"
                }
            }
        },
        "codomain" : {
            "tag" : "unlabeledProduct",
            "items" : [
                {
                    "tag" : "scalar",
                    "name" : "string"
                },
                {
                    "tag" : "scalar",
                    "name" : "string"
                },
                {
                    "tag" : "scalar",
                    "name" : "string"
                },
                {
                    "tag" : "scalar",
                    "name" : "string"
                },
                {
                    "tag" : "scalar",
                    "name" : "string"
                },
                {
                    "tag" : "scalar",
                    "name" : "string"
                },
                {
                    "tag" : "scalar",
                    "name" : "string"
                },
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
        callReturnedChainArrow_script
    )
}

/* eslint-enable */