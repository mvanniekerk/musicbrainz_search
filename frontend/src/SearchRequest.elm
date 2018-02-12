module SearchRequest exposing (..)

import Http
import Json.Encode as En
import Json.Decode as De

import Work exposing (..)

type RequestMsg
    = New (Result Http.Error Response)



type alias Response =
    { took : Int
    , total : Int
    , works : List Work
    }



getWorks : String -> String -> String -> Int -> Cmd RequestMsg
getWorks query artist composer page =
    let
        body : Http.Body
        body = Http.jsonBody <| En.object
            [ ("query", En.string query)
            , ("artistQuery", En.string artist)
            , ("composerQuery", En.string composer)
            , ("page", En.int page)
            ]

        request =
            Http.post "/api" body decodeResult
    in
        Http.send New request


decodeResult : De.Decoder Response
decodeResult =
    De.map3 Response
        (De.field "took" De.int)
        (De.at ["hits", "total"] De.int)
        (De.at ["hits", "hits"] <| De.list decodeWork)


decodeWork : De.Decoder Work
decodeWork =
    De.map4 work
        (De.field "_id" De.string)
        (De.at ["_source", "names"] <| De.list De.string)
        (De.at ["_source", "composers"] <| De.list De.string)
        (De.at ["_source", "artists"] <| De.list De.string)