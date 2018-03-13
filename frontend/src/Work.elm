module Work exposing (..)

type alias Composers = List String
type alias Artists = List String

type alias Work =
    { showMoreComposers : Bool
    , showMoreArtists : Bool
    , gid : String
    , name : List String
    , composer : Composers
    , artist : Artists
    , children : Children
    , recordings : List Recording
    }

type alias Recording =
    { name : String
    , artists : Artists
    , releases : List Release
    }

type alias Release =
    { gid : String
    , name : String
    , coverArt : Maybe String
    }

getRelease : Work -> Maybe (Release, String)
getRelease w =
    let
        recording =
            .recordings w
            |> List.head
        artist =
            recording
            |> Maybe.map .artists
            |> Maybe.andThen List.head
        release =
            recording
            |> Maybe.map .releases
            |> Maybe.andThen List.head
    in
        Maybe.map2 toTuple release artist

toTuple : a -> b -> (a,b)
toTuple a b = (a,b)

type Children = Children (List Work)

children : Children -> List Work
children child =
    case child of
        Children c -> c

work : String -> List String -> Composers -> Artists -> Children -> List Recording -> Work
work = Work False False
