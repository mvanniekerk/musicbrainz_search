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
        (De.field "total" De.int)
        (De.field "works" <| De.list decodeWork)


decodeWork : De.Decoder Work
decodeWork =
    De.map5 work
        (De.field "gid" De.string)
        (De.field "names" <| De.list De.string)
        (De.field "composers" <| De.list De.string)
        (De.field "artists" <| De.list De.string)
        (De.field "children" <| De.map Children <| De.list <| De.lazy (\_ -> decodeWork))