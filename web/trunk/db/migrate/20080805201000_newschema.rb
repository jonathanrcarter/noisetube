class Newschema < ActiveRecord::Migration
  def self.up
    rename_column :tracks, :x, :lon
    rename_column :tracks, :y, :lat
    add_column :sounds,:avgloudness,:float
    add_column :sounds,:minloudness,:float
    add_column :sounds,:maxloudness,:float
    add_column :sounds,:location,:string
    rename_column :sounds,:created, :start
    add_column :sounds,:end,:datetime
    add_column :sounds,:feeling,:integer
    #todo migrate data
    remove_column :tags,:feeling
    remove_column :tags,:lon
    remove_column :tags,:lat
    remove_column :tags,:start
    remove_column :tags,:end
  end

  def self.down
    add_column :tags,:lon, :float
    add_column :tags,:lat, :float
    add_column :tags,:start,:datetime
    add_column :tags,:end,:datetime
    add_colum :tags, :feeling,:integer
    remove_column :sounds,:feeling
    remove_column :sounds,:end
    rename_column :sounds,:start,:created
    remove_column :sounds,:maxloudness
    remove_column :sounds,:avgloudness
    remove_column :sounds,:minloudness
    rename_column :tracks, :lon, :x
    rename_column :tracks, :lat, :y
  end
end
