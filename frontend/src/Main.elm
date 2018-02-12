import Html exposing (div, label, input, text, button, program, Html)
import Html.Events as Events exposing (onClick, onInput)
import Html.Attributes as Attr exposing (href, attribute)
import Http

import SearchResult exposing (SearchResult, ResultMsg, updateResult, searchResultView)
import SearchRequest exposing (RequestMsg(..), getWorks, Response)
import Utils exposing (onEnter)
import FieldSearch exposing (..)

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
    , fieldSearch : FieldSearch
    }




init : (Model, Cmd Msg)
init =
    (Model "" "" Nothing fieldSearch, Cmd.none)

-- UPDATE

type Msg
    = Search
    | RequestMsg RequestMsg
    | Query String
    | ResultMsg ResultMsg
    | FieldMsg FieldMsg





update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
    case msg of
        Search ->
            ( { model | result = Nothing }, Cmd.map RequestMsg <| getWorks model.query 1 )

        Query str ->
            ( { model | query = str }, Cmd.none)

        ResultMsg msg ->
            case model.result of
                Nothing -> ({ model | message = "Illegal message, can not change results when there are none" }, Cmd.none)
                Just result ->
                    let
                        srm : (SearchResult, Cmd ResultMsg)
                        srm = updateResult msg result

                        newResult = Tuple.first srm

                        newMsg : Cmd Msg
                        newMsg = Tuple.second srm |> Cmd.map ResultMsg
                    in
                        ({ model | result = Just newResult }, newMsg )

        FieldMsg msg ->
            let
                field : (FieldSearch, Cmd FieldMsg)
                field = fieldUpdate msg model.fieldSearch

                newMsg : Cmd Msg
                newMsg =
                    case msg of
                        SearchFromField -> Cmd.map RequestMsg <| getWorks model.query 1
                        _ -> Tuple.second field |> Cmd.map FieldMsg
            in
                ({ model | fieldSearch = Tuple.first field }, newMsg )

        RequestMsg (New (Ok response)) ->
            ( { model | result = Just <| result model.query response }, Cmd.none)

        RequestMsg (New (Err (Http.BadStatus resp))) ->
            let
                code = resp.status.code
                msg = case code of
                    500 ->
                        "Search server gave status code 500. This probably means that the database is not running."
                    _  ->
                        "Something went wrong, status code " ++ (toString code)
            in
                ( { model | message =  msg }, Cmd.none )

        RequestMsg (New (Err (Http.BadPayload msg _))) ->
            ( { model | message = msg }, Cmd.none )

        RequestMsg (New (Err _)) ->
            (model, Cmd.none)


result : String -> Response -> SearchResult
result query response = SearchResult 1 query response.took response.total response.works

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
                , input
                    [ onInput Query
                    , onEnter Search
                    , attribute "id" "search-bar"
                    , Attr.type_ "search"
                    , Attr.placeholder "Search"
                    , Attr.autofocus True
                    ] []
                , button [ onClick Search, attribute "class" "search-button" ] []
                ]
            , fieldView model.fieldSearch |> Html.map FieldMsg
            , Maybe.withDefault (div [] [])
            <| Maybe.map (\a -> a |> searchResultView |> Html.map ResultMsg) result
        ]