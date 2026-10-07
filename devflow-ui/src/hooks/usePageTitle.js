import { useEffect } from "react";


export function usePageTitle(title) {
    useEffect(() => {
        document.title = title ? `${title} - DevFlow` : `DeVFlow - Project Management for Dev Teams`;
        return () => {
            document.title = 'DevFlow - Project Management for Dev Teams';
        }
    }, [title]);
}