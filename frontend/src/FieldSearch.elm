module FieldSearch exposing (..)

import Html exposing (..)
import Html.Attributes as Attr
import Html.Events exposing (onClick, onInput)

import Utils exposing (onEnter)


-- MODEL

type alias FieldSearch =
    { open : Bool
    , artistQuery : String
    , composerQuery : String
    }

fieldSearch : FieldSearch
fieldSearch = FieldSearch False "" ""

-- UPDATE

type FieldMsg
    = Toggle
    | ArtistQuery String
    | ComposerQuery String
    | SearchFromField

fieldUpdate : FieldMsg -> FieldSearch -> (FieldSearch, Cmd FieldMsg)
fieldUpdate msg model =
    case msg of
        Toggle ->
            ({ model | open = not model.open }, Cmd.none )

        ArtistQuery query ->
            ({ model | artistQuery = query }, Cmd.none )

        ComposerQuery query ->
            ({ model | composerQuery = query }, Cmd.none )

        SearchFromField -> (model, Cmd.none)


-- VIEW

fieldView : FieldSearch -> Html FieldMsg
fieldView model =
    let
        fields =
            case model.open of
                True -> openView model
                False -> div [] []

        fieldHeader =
            div [ Attr.class "field-header", onClick Toggle ]
                [ h4 [ Attr.class "field-title" ] [ text "Field Search" ]
                , button [ Attr.class "field-button" ] []
                ]
    in
        div [ Attr.class "field-view" ]
            [ fieldHeader
            , fields
            ]

openView : FieldSearch -> Html FieldMsg
openView model =
    div [ Attr.class "fields" ]
    [ div
        [ Attr.class "search-field" ]
        [ p [] [ text "Composer:" ]
        , input
            [ onInput ComposerQuery
            , onEnter SearchFromField
            , Attr.value model.composerQuery
            ] []
        ]
    , div
        [ Attr.class "search-field" ]
        [ p [] [ text "Artist:" ]
        , input
            [ onInput ArtistQuery
            , onEnter SearchFromField
            , Attr.value model.artistQuery
            ] []
        ]
    ]