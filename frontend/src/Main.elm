import Html exposing (..)
import Html.Events exposing (onClick, onInput)
import Html.Attributes exposing (href, attribute)
import Http
import Json.Decode as Decode
import Tuple exposing (first)

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
    , works : List Work
    }

type alias Work =
    { gid : String
    , length : Int
    , name : String
    , composer : String
    , tfIdf : Float
    , terms : List (String, Int)
    }

type alias Term =
    { something : String
    , freq : Int
    }

init : (Model, Cmd Msg)
init =
    (Model "" "" [], Cmd.none)

-- UPDATE

type Msg
    = Search
    | New (Result Http.Error (List Work))
    | Query String

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
    case msg of
        Search ->
            (model, getWorks model.query)

        Query str ->
            ( { model | query = str }, Cmd.none)

        New (Ok newWorks) ->
            ( { model | works = newWorks }, Cmd.none)

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

        New (Err _) ->
            (model, Cmd.none)

getWorks : String -> Cmd Msg
getWorks query =
    let
        url =
            "/api/" ++ query ++ "/1"

        request =
            Http.get url decodeResult
    in
        Http.send New request

decodeResult : Decode.Decoder (List Work)
decodeResult =
    Decode.list decodeWork


decodeWork : Decode.Decoder Work
decodeWork =
    Decode.map6 Work
        (Decode.field "gid" Decode.string)
        (Decode.field "length" Decode.int)
        (Decode.field "name" Decode.string)
        (Decode.field "composer" Decode.string)
        (Decode.field "tfIdf" Decode.float)
        (Decode.field "terms" (Decode.keyValuePairs Decode.int))

-- VIEW

view : Model -> Html Msg
view model =
    div []
        [ div [] [text model.message]
        , input [ onInput Query ] []
        , button [ onClick Search ] [ text "Search" ]
        , div [] (List.map workView model.works)
        ]


workView : Work -> Html Msg
workView work =
    div [ attribute "class" "work" ]
        [ a [ href ("https://musicbrainz.org/work/" ++ work.gid) ] [ text work.name ]
        , p [] [ text work.composer ]
        , p [] (List.map (\t -> text ((first t) ++ ", ")) work.terms)
        ]