import Html exposing (..)
import Html.Events as Events exposing (onClick, onInput)
import Html.Attributes as Attr exposing (href, attribute)
import Http
import Json.Decode as Decode
import Json.Encode as En
import Tuple

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
    { page : Int
    , took : Int
    , total : Int
    , works : List Work
    }

type alias Response =
    { took : Int
    , total : Int
    , works : List Work
    }

type alias Composers = List String
type alias Artists = List String

type alias Work =
    { gid : String
    , name : List String
    , composer : Composers
    , artist : Artists
    }

init : (Model, Cmd Msg)
init =
    (Model "" "" Nothing, Cmd.none)

-- UPDATE

type Msg
    = Search
    | New (Result Http.Error Response)
    | Query String
    | LoadPage Int

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
    case msg of
        Search ->
            ( { model | result = Nothing }, getWorks model.query 1 )

        LoadPage newPage ->
            case model.result of
                Nothing ->
                    ( { model | message = "Illegal state, request new page without results" }, Cmd.none)
                Just result ->
                    ( { model | result = Just { result | page = newPage } }, getWorks model.query newPage )

        Query str ->
            ( { model | query = str }, Cmd.none)

        New (Ok response) ->
            ( { model | result = Just <| addResult model.result response }, Cmd.none)

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

addResult : Maybe SearchResult -> Response -> SearchResult
addResult previousResult response =
    case previousResult of
        Nothing -> SearchResult 1 response.took response.total response.works
        Just result ->
            let
                newWorks : List Work
                newWorks = result.works ++ response.works
            in
                { result | works = newWorks }




getWorks : String -> Int -> Cmd Msg
getWorks query page =
    let
        body : Http.Body
        body = Http.jsonBody <| En.object
            [ ("query", En.string query)
            , ("page", En.int page)
            ]

        request =
            Http.post "/api" body decodeResult
    in
        Http.send New request


decodeResult : Decode.Decoder Response
decodeResult =
    Decode.map3 Response
        (Decode.field "took" Decode.int)
        (Decode.at ["hits", "total"] Decode.int)
        (Decode.at ["hits", "hits"] <| Decode.list decodeWork)


decodeWork : Decode.Decoder Work
decodeWork =
    Decode.map4 Work
        (Decode.field "_id" Decode.string)
        (Decode.at ["_source", "names"] <| Decode.list Decode.string)
        (Decode.at ["_source", "composers"] <| Decode.list Decode.string)
        (Decode.at ["_source", "artists"] <| Decode.list Decode.string)

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
            , Maybe.withDefault (div [] []) <| Maybe.map searchResultView result
        ]

{-
https://github.com/evancz/elm-todomvc/blob/master/Todo.elm#L237
-}
onEnter : Msg -> Attribute Msg
onEnter msg =
    let
        isEnter code =
            if code == 13 then
                Decode.succeed msg
            else
                Decode.fail "not Enter"
    in
        Events.on "keydown" (Decode.andThen isEnter Events.keyCode)


searchResultView : SearchResult -> Html Msg
searchResultView sr =
    div [ attribute "class" "search-result" ]
        [ p [ attribute "class" "status-message" ]
            [ text <| toString sr.total ++ " results, took " ++ toString sr.took ++ " ms" ]
        , div [] <| List.map workView sr.works
        , moreResultsButton sr
        ]


moreResultsButton : SearchResult -> Html Msg
moreResultsButton sr =
    if sr.total > sr.page * 10 then
         button [ onClick <| LoadPage <| sr.page + 1] [ text "Load More Results"]
    else if sr.total > 0 then
        p [ attribute "class" "status-message" ] [text "There are no more results"]
    else
        p [] []

workView : Work -> Html Msg
workView work =
    let
        name : String
        name = Maybe.withDefault "no name" <| List.head work.name

        artist : String
        artist = Maybe.withDefault "no artist" <| List.head work.artist
    in
        div [ attribute "class" "work" ]
            [ a
                [ href ("https://musicbrainz.org/work/" ++ work.gid)
                , Attr.class "work-link"
                ]
                [ text name ]
            , composerView work.composer
            , p [] [ text artist ]
            ]

composerView : Composers -> Html Msg
composerView =
    listView 1 "Composer"

listView : Int -> String -> List String -> Html Msg
listView showN name list =
    let
        grouped = sortByOccurrence list
    in
        div [ Attr.class "listing" ]
            [ p [ text <| name ++ ": " ]
            , ul [] <| List.map (\a -> li [] [text a]) (List.take showN <| grouped)
            , a [ Attr.href "#"] [ text "More" ]
            ]


-- utility functions


{-| Group values, keeping track off the number of occurrences

    groupByValue ["hey", "bye", "hey", "bye", "no way"] == [("bye",2),("hey",2),("no way",1)]

    Values are first sorted, to make the function run in O(N log N) time.
-}
groupByValue : List comparable -> List (comparable, Int)
groupByValue list =
    let
        sorted = List.sort list

        groupBy inp acc =
            let
                mhead = List.head acc
                tail = Maybe.withDefault [] <| List.tail acc
            in
                case mhead of
                    Nothing -> [(inp, 1)]
                    Just head ->
                        if Tuple.first head == inp then
                            Tuple.mapSecond (\x -> x+1) head :: tail
                        else
                            (inp, 1) :: acc
    in
        List.foldr groupBy [] sorted


{-| Sort values by occurrence.

    sortByOccurrence ["hey", "bye", "hey", "bye", "no way"] = ["bye", "hey", "no way"]

    Values that occur the same amount of times are sorted normally
-}
sortByOccurrence : List comparable -> List comparable
sortByOccurrence list =
    let
        sortBySecond : List (comparable, Int) -> List (comparable, Int)
        sortBySecond = List.sortWith (\a b -> descending (Tuple.second a) (Tuple.second b))
        takeFirst = List.map Tuple.first
    in
        list
        |> groupByValue
        |> sortBySecond
        |> takeFirst


{-| Sort descending instead of ascending.

    sortWith descending [1, 2, 3] == [3, 2, 1]
-}
descending : comparable -> comparable -> Order
descending a b =
    case compare a b of
        LT -> GT
        EQ -> EQ
        GT -> LT