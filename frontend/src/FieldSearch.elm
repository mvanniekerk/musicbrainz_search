module FieldSearch exposing (..)

import Html exposing (..)
import Html.Attributes as Attr
import Html.Events exposing (onClick)


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

fieldUpdate : FieldMsg -> FieldSearch -> (FieldSearch, Cmd FieldMsg)
fieldUpdate msg model =
    case msg of
        Toggle ->
            ({ model | open = not model.open }, Cmd.none )

        ArtistQuery query ->
            ({ model | artistQuery = query }, Cmd.none )

        ComposerQuery query ->
            ({ model | composerQuery = query }, Cmd.none )


-- VIEW

fieldView : FieldSearch -> Html FieldMsg
fieldView model =
    let
        fields =
            case model.open of
                True -> openView model
                False -> div [] []

        fieldHeader =
            div [ Attr.class "field-header" ]
                [ h4 [ Attr.class "field-title" ] [ text "Field Search" ]
                , button [ Attr.class "field-button",  onClick Toggle ] []
                ]
    in
        div [ Attr.class "field-view" ]
            [ fieldHeader
            , fields
            ]

openView : FieldSearch -> Html FieldMsg
openView model =
    p [] [ text "open" ]