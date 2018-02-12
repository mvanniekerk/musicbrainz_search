module SearchResult exposing (..)

import Html exposing (..)
import Html.Attributes as Attr
import Html.Events exposing (onClick)

import Work exposing (..)
import Utils exposing (sortByOccurrence)
import SearchRequest exposing (RequestMsg(..), getWorks, Response)

-- MODEL

type alias SearchResult =
    { page : Int
    , query : String
    , took : Int
    , total : Int
    , works : List Work
    }



-- UPDATE
type ResultMsg
    = ShowComposers Gid Bool
    | ShowArtists Gid Bool
    | LoadPage Int
    | ResultRequest RequestMsg

type alias Gid = String


updateResult : ResultMsg -> SearchResult -> (SearchResult, Cmd ResultMsg)
updateResult msg model =
    case msg of
        ShowComposers gid newVal ->
            (changeWorkField model gid <| setMoreComposers newVal, Cmd.none)

        ShowArtists gid newVal ->
            (changeWorkField model gid <| setMoreArtists newVal, Cmd.none)

        LoadPage newPage ->
            ( { model | page = newPage }, Cmd.map ResultRequest <| getWorks model.query newPage )

        ResultRequest (New (Ok response)) ->
            let
                newWorks : List Work
                newWorks = model.works ++ response.works
            in
                ( { model | works = newWorks }, Cmd.none )

        ResultRequest (New (Err _)) ->
            ( model, Cmd.none )


setMoreComposers : Bool -> Work -> Work
setMoreComposers to work = { work | showMoreComposers = to }

setMoreArtists : Bool -> Work -> Work
setMoreArtists to work = { work | showMoreArtists = to }

changeWorkField : SearchResult -> String -> (Work -> Work) -> SearchResult
changeWorkField result gid setter =
    let
        nWork : Work -> Work
        nWork w =
            if w.gid == gid then
                setter w
            else
                w

        newWorks = List.map nWork result.works
    in
        { result | works = newWorks }


-- VIEW

searchResultView : SearchResult -> Html ResultMsg
searchResultView sr =
    div [ Attr.class "search-result" ]
        [ p [ Attr.class "status-message" ]
            [ text <| toString sr.total ++ " results, took " ++ toString sr.took ++ " ms" ]
        , div [] <| List.map workView sr.works
        , moreResultsButton sr
        ]


moreResultsButton : SearchResult -> Html ResultMsg
moreResultsButton sr =
    if sr.total > sr.page * 10 then
         button [ onClick <| LoadPage <| sr.page + 1] [ text "Load More Results"]
    else if sr.total > 0 then
        p [ Attr.class "status-message" ] [text "There are no more results"]
    else
        p [] []

workView : Work -> Html ResultMsg
workView work =
    let
        name : String
        name = Maybe.withDefault "no name" <| List.head work.name

        artistShow = if work.showMoreArtists then 100 else 5
        composerShow = if work.showMoreComposers then 100 else 1
    in
        div [ Attr.class "work" ]
            [ a
                [ Attr.href ("https://musicbrainz.org/work/" ++ work.gid)
                , Attr.class "work-link"
                ]
                [ text name ]
            , composerView work.composer work.gid work.showMoreComposers
            , artistView work.artist work.gid work.showMoreArtists
            ]

composerView : List String -> String -> Bool -> Html ResultMsg
composerView = listView "Composer" ShowComposers 1

artistView : List String -> String -> Bool -> Html ResultMsg
artistView = listView "Artists" ShowArtists 5

listView :
    String -> (String -> Bool -> ResultMsg) -> Int ->
    List String -> String -> Bool -> Html ResultMsg
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