# cutis
CUTiS: Online Clustering of Trajectory Data Stream

Recent approaches for online clustering of moving objects location are restricted to instantaneous positions. Subsequently, they fail to capture the behavior of moving objects over time. By continuously tracking sub-trajectories of moving object at each time window, it becomes possible to gain insight on the current behavior and potentially detect mobility patterns in real time. Our framework CUTiS implements an incremental algorithm for discovering and maintaining the density-based clusters in trajectory data streams, while tracking the evolution of the clusters. CUTiS presents an indexing structure for sub-trajectory data based on a space-filling curve. This index improves the performance of our approach without losing quality in the clusters results. CUTiS is also suitable to discover others mobility patterns, like flocks.

Some videos about CUTiS' demonstration: https://www.dropbox.com/sh/mn52msj7o3h8cvr/AABzQaBpkrP0LwWBU9N38T5Ma?dl=0
