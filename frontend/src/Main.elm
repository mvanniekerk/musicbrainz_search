import Html exposing (..)
import Html.Events exposing (onClick)
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
    , works : List Work
    }

type alias Work =
    { gid : String
    }

init : (Model, Cmd Msg)
init =
    (Model "hello" [], Cmd.none)

-- UPDATE

type Msg
    = Search
    | New (Result Http.Error (List Work))

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
    case msg of
        Search ->
            (model, getWorks model.query)

        New (Ok newWorks) ->
            ( { model | works = newWorks }, Cmd.none)

        New (Err Http.NetworkError) ->
            ( { model | query = "networkError" }, Cmd.none)

        New (Err (Http.BadStatus resp)) ->
            ( { model | query = resp.url}, Cmd.none )

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
    Decode.list (Decode.map Work (Decode.field "gid" Decode.string))

-- VIEW

view : Model -> Html Msg
view model =
    div []
        [ button [ onClick Search ] [ text "Search" ]
        , text model.query
        , div [] (List.map (\w -> w.gid |> text) model.works)
        ]