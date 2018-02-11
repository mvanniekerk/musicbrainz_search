import Html exposing (..)
import Html.Events as Events exposing (onClick, onInput)
import Html.Attributes as Attr exposing (href, attribute)
import Http
import Json.Decode as De
import Json.Encode as En

import Utils exposing (sortByOccurrence)

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
    { showMoreComposers : Bool
    , showMoreArtists : Bool
    , gid : String
    , name : List String
    , composer : Composers
    , artist : Artists
    }

work : String -> List String -> Composers -> Artists -> Work
work = Work False False

init : (Model, Cmd Msg)
init =
    (Model "" "" Nothing, Cmd.none)

-- UPDATE

type Msg
    = Search
    | New (Result Http.Error Response)
    | Query String
    | LoadPage Int
    | ShowComposers String Bool
    | ShowArtists String Bool

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

        ShowComposers gid newVal ->
            (changeWorkField model gid <| setMoreComposers newVal, Cmd.none)

        ShowArtists gid newVal ->
            (changeWorkField model gid <| setMoreArtists newVal, Cmd.none)

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


setMoreComposers : Bool -> Work -> Work
setMoreComposers to work = { work | showMoreComposers = to }

setMoreArtists : Bool -> Work -> Work
setMoreArtists to work = { work | showMoreArtists = to }

changeWorkField : Model -> String -> (Work -> Work) -> Model
changeWorkField model gid setter =
    let
        nWork : Work -> Work
        nWork w =
            if w.gid == gid then
                setter w
            else
                w

        nSR : SearchResult -> SearchResult
        nSR sr =
            let
                newWorks = List.map nWork sr.works
            in
                { sr | works = newWorks }

        results : Maybe SearchResult
        results = Maybe.map nSR model.result
    in
        { model | result = results }


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
                De.succeed msg
            else
                De.fail "not Enter"
    in
        Events.on "keydown" (De.andThen isEnter Events.keyCode)


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

        artistShow = if work.showMoreArtists then 100 else 5
        composerShow = if work.showMoreComposers then 100 else 1
    in
        div [ attribute "class" "work" ]
            [ a
                [ href ("https://musicbrainz.org/work/" ++ work.gid)
                , Attr.class "work-link"
                ]
                [ text name ]
            , composerView work.composer work.gid work.showMoreComposers
            , artistView work.artist work.gid work.showMoreArtists
            ]

composerView : List String -> String -> Bool -> Html Msg
composerView = listView "Composer" ShowComposers 1

artistView : List String -> String -> Bool -> Html Msg
artistView = listView "Artists" ShowArtists 5

listView :
    String -> (String -> Bool -> Msg) -> Int ->
    List String -> String -> Bool -> Html Msg
listView name msg showN list gid showMore =
    let
        grouped = sortByOccurrence list
        count = List.length grouped

        take =
            if showMore then
                grouped
            else
                (List.take showN <| grouped)
    in
        div [ Attr.class "listing" ]
            [ p [] [ text <| name ++ ": " ]
            , ul [] <| List.map (\a -> li [] [text a]) take
            , if count <= showN then
                a [] []
              else if showMore then
                a [ onClick <| msg gid False ] [ text "Less" ]
              else
                a [ onClick <| msg gid True ] [ text "More" ]
            ]


-- utility functions


