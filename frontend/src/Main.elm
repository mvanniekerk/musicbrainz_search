import Html exposing (..)
import Html.Events exposing (onClick, onInput)
import Html.Attributes as Attr exposing (href, attribute)
import Http
import Json.Decode as Decode

main : Program Never Model Msg
main = program
    { init = init
    , update = update
    , view = view
    , subscriptions = \_ -> Sub.none
    }

-- MODEl

type alias Model =
    { query : String
    , message : String
    , result : Maybe SearchResult
    }

type alias SearchResult =
    { took : Int
    , total : Int
    , works : List Work
    }

type alias Work =
    { gid : String
    , name : List String
    , composer : List String
    }

init : (Model, Cmd Msg)
init =
    (Model "" "" Nothing, Cmd.none)

-- UPDATE

type Msg
    = Search
    | New (Result Http.Error SearchResult)
    | Query String

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
    case msg of
        Search ->
            ( model, getWorks model.query)

        Query str ->
            ( { model | query = str }, Cmd.none)

        New (Ok searchResult) ->
            ( { model | result = Just searchResult }, Cmd.none)

        New (Err (Http.BadStatus resp)) ->
            let
                code = resp.status.code
                msg = case code of
                    500 ->
                        "Search server gave status code 500. This probably means that the database is not running."
                    _  ->
                        "Something went wrong, status code " ++ (toString code)
            in
                ( { model | message =  msg }, Cmd.none )

        New (Err (Http.BadPayload msg _)) ->
            ( { model | message = msg }, Cmd.none )

        New (Err _) ->
            (model, Cmd.none)

getWorks : String -> Cmd Msg
getWorks query =
    let
        words : List String
        words = String.split " " query

        url =
            "/api/" ++ query

        request =
            Http.get url decodeResult
    in
        Http.send New request


decodeResult : Decode.Decoder SearchResult
decodeResult =
    Decode.map3 SearchResult
        (Decode.field "took" Decode.int)
        (Decode.at ["hits", "total"] Decode.int)
        (Decode.at ["hits", "hits"] <| Decode.list decodeWork)


decodeWork : Decode.Decoder Work
decodeWork =
    Decode.map3 Work
        (Decode.field "_id" Decode.string)
        (Decode.at ["_source", "names"] <| Decode.list Decode.string)
        (Decode.at ["_source", "composers"] <| Decode.list Decode.string)

-- VIEW

view : Model -> Html Msg
view model =
    let
        result : Maybe SearchResult
        result = model.result
    in
        div []
            [ div
                [attribute "class" "search-wrapper"]
                [ div [] [text model.message]
                , label [attribute "class" "search-label" ] []
                , input [ onInput Query
                    , attribute "id" "search-bar"
                    , Attr.type_ "search"
                    , Attr.placeholder "Search"] []
                , button [ onClick Search, attribute "class" "search-button" ] []
                ]
            , Maybe.withDefault (div [] []) <| Maybe.map searchResultView result
        ]


searchResultView : SearchResult -> Html Msg
searchResultView sr =
    div [ attribute "class" "search-result" ]
        [ p [ attribute "class" "status-message" ]
            [ text <| toString sr.total ++ " results, took " ++ toString sr.took ++ " ms" ]
        , div [] <| List.map workView sr.works
        ]

workView : Work -> Html Msg
workView work =
    let
        name : String
        name = Maybe.withDefault "no name" <| List.head work.name

        composer : String
        composer = Maybe.withDefault "no composer" <| List.head work.composer
    in
        div [ attribute "class" "work" ]
            [ a
                [ href ("https://musicbrainz.org/work/" ++ work.gid)
                , Attr.class "work-link"
                ]
                [ text name ]
            , p [] [ text composer ]
            ]